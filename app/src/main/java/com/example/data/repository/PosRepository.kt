package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CashDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.PosDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ProductVariantDao
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockBalanceEntity
import com.example.data.local.entity.StockMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

data class CartItem(
    val productId: String,
    val variantId: String? = null,
    val productName: String,
    val variantName: String? = null,
    val unitPrice: Long,
    val quantity: Int,
    val discountAmount: Long = 0L,
    val trackStock: Boolean = true,
    val allowNegativeStock: Boolean = false,
    val barcode: String? = null
) {
    val subtotal: Long
        get() = (unitPrice * quantity) - discountAmount
}

data class CheckoutReceipt(
    val sale: SaleEntity,
    val items: List<SaleItemEntity>,
    val payment: PaymentEntity,
    val cashTransaction: CashTransactionEntity?
)

class PosRepository(
    private val database: AppDatabase,
    private val posDao: PosDao,
    private val inventoryDao: InventoryDao,
    private val cashDao: CashDao,
    private val productDao: ProductDao,
    private val productVariantDao: ProductVariantDao = database.productVariantDao(),
    private val customerDao: CustomerDao = database.customerDao()
) {
    // Double checkout guard: prevent concurrent checkouts in repository
    private val isCheckingOut = AtomicBoolean(false)

    fun getSalesFlow(businessId: String): Flow<List<SaleEntity>> {
        return posDao.getSalesForBusiness(businessId)
    }

    suspend fun getSaleById(id: String): SaleEntity? = withContext(Dispatchers.IO) {
        posDao.getSaleById(id)
    }

    suspend fun getSaleItems(saleId: String): List<SaleItemEntity> = withContext(Dispatchers.IO) {
        posDao.getSaleItems(saleId)
    }

    suspend fun getPaymentForSale(saleId: String): PaymentEntity? = withContext(Dispatchers.IO) {
        posDao.getPaymentForSale(saleId)
    }

    fun getCashBalanceFlow(businessId: String): Flow<Long> {
        return cashDao.getCashBalanceFlow(businessId)
    }

    suspend fun getCashBalance(businessId: String): Long = withContext(Dispatchers.IO) {
        cashDao.getCashBalanceSync(businessId)
    }

    suspend fun generateInvoiceNumber(businessId: String): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val prefix = "INV/$dateString/"

        val lastInvoice = posDao.getLastInvoiceNumberToday(businessId, "$prefix%")
        val nextSeq = if (lastInvoice != null && lastInvoice.startsWith(prefix)) {
            val seqStr = lastInvoice.removePrefix(prefix)
            (seqStr.toIntOrNull() ?: 0) + 1
        } else {
            1
        }
        "$prefix%04d".format(nextSeq)
    }

    /**
     * Executes complete atomic checkout with strict validation:
     * 1. Protect against double checkout via AtomicBoolean guard.
     * 2. Consolidate duplicate cart rows by (productId, variantId).
     * 3. Validate product and variant ownership against active business.
     * 4. Query authoritative trackStock & allowNegativeStock from Room (do not trust UI state).
     * 5. Validate total requested stock sufficiency.
     * 6. Validate payment method and amount paid.
     * 7. Only create CashTransaction for CASH payment method.
     * 8. Do not generate fake payment references.
     */
    suspend fun processCheckout(
        businessId: String,
        cartItems: List<CartItem>,
        customerId: String? = null,
        customerName: String = "Pelanggan Umum",
        orderDiscount: Long = 0L,
        taxAmount: Long = 0L,
        paymentMethod: String, // CASH, BANK_TRANSFER, QRIS, OTHER
        amountPaid: Long,
        notes: String = "",
        paymentReference: String? = null
    ): Result<CheckoutReceipt> = withContext(Dispatchers.IO) {
        if (!isCheckingOut.compareAndSet(false, true)) {
            return@withContext Result.failure(
                IllegalStateException("Transaksi checkout sedang diproses. Mohon tunggu.")
            )
        }

        try {
            if (cartItems.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Keranjang belanja masih kosong"))
            }

            val normalizedMethod = paymentMethod.trim().uppercase()
            val allowedMethods = setOf("CASH", "BANK_TRANSFER", "QRIS", "OTHER")
            if (normalizedMethod !in allowedMethods) {
                return@withContext Result.failure(IllegalArgumentException("Metode pembayaran tidak valid"))
            }

            runCatching {
                database.withTransaction {
                    // STEP 0: Validate Customer Ownership if provided
                    if (customerId != null) {
                        val cust = customerDao.getCustomerById(customerId)
                        if (cust == null || cust.businessId != businessId) {
                            throw IllegalArgumentException("Pelanggan tidak valid atau bukan milik usaha aktif.")
                        }
                    }

                    // STEP 1: Consolidate cart rows by (productId, variantId) to prevent stock bypass
                    val consolidatedMap = cartItems.groupBy { it.productId to it.variantId }

                    // STEP 2: Authoritative Product Ownership & Stock Validation
                    for ((key, group) in consolidatedMap) {
                        val (productId, variantId) = key
                        val totalRequestedQty = group.sumOf { it.quantity }
                        if (totalRequestedQty <= 0) {
                            throw IllegalArgumentException("Jumlah barang pada keranjang harus lebih dari 0")
                        }

                        // Check product exists and belongs to this business
                        val product = productDao.getProductByIdForBusiness(businessId, productId)
                            ?: throw IllegalArgumentException("Produk \"${group.first().productName}\" tidak ditemukan atau bukan milik usaha aktif.")

                        // Check variant belongs to this business and product if present
                        var variant: ProductVariantEntity? = null
                        if (variantId != null) {
                            val v = productVariantDao.getVariantById(variantId)
                            if (v == null || v.businessId != businessId || v.productId != product.id) {
                                throw IllegalArgumentException("Varian produk tidak valid atau bukan milik usaha aktif.")
                            }
                            variant = v
                        }

                        // Authoritative stock check from database
                        if (product.trackStock) {
                            val balance = inventoryDao.getStockBalance(businessId, productId, variantId)
                            val currentStock = balance?.quantity ?: 0
                            if (currentStock < totalRequestedQty && !product.allowNegativeStock) {
                                val displayName = if (variant != null) {
                                    "${product.productName} (${variant.variantName})"
                                } else {
                                    product.productName
                                }
                                throw IllegalStateException("Stok tidak mencukupi untuk \"$displayName\". Sisa stok: $currentStock, diminta: $totalRequestedQty.")
                            }
                        }
                    }

                    // STEP 3: Financial Calculations & Payment Validation
                    val rawSubtotal = cartItems.sumOf { it.subtotal }
                    val totalAmount = (rawSubtotal - orderDiscount + taxAmount).coerceAtLeast(0L)

                    val changeAmount: Long
                    if (normalizedMethod == "CASH") {
                        if (amountPaid < totalAmount) {
                            throw IllegalArgumentException("Nominal pembayaran tunai kurang dari total tagihan")
                        }
                        changeAmount = amountPaid - totalAmount
                    } else {
                        // Non-CASH payments: must cover total, no cash change
                        if (amountPaid < totalAmount) {
                            throw IllegalArgumentException("Nominal pembayaran kurang dari total tagihan")
                        }
                        changeAmount = 0L
                    }

                    val saleId = UUID.randomUUID().toString()
                    val invoiceNumber = generateInvoiceNumber(businessId)

                    // STEP 4: Insert Sale Record
                    val sale = SaleEntity(
                        id = saleId,
                        businessId = businessId,
                        invoiceNumber = invoiceNumber,
                        customerId = customerId,
                        customerName = customerName.ifBlank { "Pelanggan Umum" },
                        subtotal = rawSubtotal,
                        discountAmount = orderDiscount,
                        taxAmount = taxAmount,
                        totalAmount = totalAmount,
                        paymentMethod = normalizedMethod,
                        paymentStatus = "PAID",
                        notes = notes,
                        createdAt = System.currentTimeMillis()
                    )
                    posDao.insertSale(sale)

                    // STEP 5: Insert Sale Items
                    val saleItems = cartItems.map { item ->
                        SaleItemEntity(
                            id = UUID.randomUUID().toString(),
                            saleId = saleId,
                            businessId = businessId,
                            productId = item.productId,
                            variantId = item.variantId,
                            productName = item.productName,
                            variantName = item.variantName,
                            unitPrice = item.unitPrice,
                            quantity = item.quantity,
                            discountAmount = item.discountAmount,
                            subtotal = item.subtotal
                        )
                    }
                    posDao.insertSaleItems(saleItems)

                    // STEP 6: Insert Payment (Do NOT invent fake references like REF-XXXXXXXX)
                    val cleanReference = paymentReference?.trim()?.ifBlank { null }

                    val payment = PaymentEntity(
                        id = UUID.randomUUID().toString(),
                        saleId = saleId,
                        businessId = businessId,
                        paymentMethod = normalizedMethod,
                        amountPaid = amountPaid,
                        changeAmount = changeAmount,
                        referenceNumber = cleanReference,
                        status = "SUCCESS",
                        createdAt = System.currentTimeMillis()
                    )
                    posDao.insertPayment(payment)

                    // STEP 7: Authoritative Stock Reduction & Movement Recording
                    for ((key, group) in consolidatedMap) {
                        val (productId, variantId) = key
                        val product = productDao.getProductByIdForBusiness(businessId, productId)!!
                        if (product.trackStock) {
                            val totalQty = group.sumOf { it.quantity }
                            val currentBalanceEntity = inventoryDao.getStockBalance(businessId, productId, variantId)
                            val currentQty = currentBalanceEntity?.quantity ?: 0
                            val newQty = currentQty - totalQty

                            val balanceId = currentBalanceEntity?.id ?: UUID.randomUUID().toString()
                            val updatedBalance = StockBalanceEntity(
                                id = balanceId,
                                businessId = businessId,
                                productId = productId,
                                variantId = variantId,
                                quantity = newQty,
                                updatedAt = System.currentTimeMillis()
                            )
                            inventoryDao.insertOrUpdateBalance(updatedBalance)

                            val stockMovement = StockMovementEntity(
                                id = UUID.randomUUID().toString(),
                                businessId = businessId,
                                productId = productId,
                                variantId = variantId,
                                type = "SALE",
                                quantity = -totalQty,
                                balanceBefore = currentQty,
                                balanceAfter = newQty,
                                referenceType = "SALE",
                                referenceId = saleId,
                                notes = "Penjualan Kasir $invoiceNumber",
                                createdAt = System.currentTimeMillis()
                            )
                            inventoryDao.insertStockMovement(stockMovement)
                        }
                    }

                    // STEP 8: Record Cash Transaction (Only for CASH payments)
                    val cashTx = if (normalizedMethod == "CASH") {
                        val cashEntity = CashTransactionEntity(
                            id = UUID.randomUUID().toString(),
                            businessId = businessId,
                            type = "IN",
                            category = "PENJUALAN",
                            amount = totalAmount,
                            referenceType = "SALE",
                            referenceId = saleId,
                            notes = "Kas Masuk Penjualan $invoiceNumber",
                            createdAt = System.currentTimeMillis()
                        )
                        cashDao.insertCashTransaction(cashEntity)
                        cashEntity
                    } else {
                        // Non-cash: do NOT create cash transaction, do NOT modify cash balance
                        null
                    }

                    CheckoutReceipt(
                        sale = sale,
                        items = saleItems,
                        payment = payment,
                        cashTransaction = cashTx
                    )
                }
            }
        } finally {
            isCheckingOut.set(false)
        }
    }
}
