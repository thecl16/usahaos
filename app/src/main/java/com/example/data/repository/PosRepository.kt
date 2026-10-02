package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CashDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.PosDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.PaymentEntity
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
    private val productDao: ProductDao
) {
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
     * Executes complete atomic checkout:
     * 1. Validate stock sufficiency for trackable items (prevent checkout if stock insufficient unless allowNegativeStock is true)
     * 2. Insert SaleEntity
     * 3. Insert SaleItemEntity for each item
     * 4. Insert PaymentEntity
     * 5. Deduct stock and insert StockMovementEntity for trackable items
     * 6. Insert CashTransactionEntity (Cash In) if payment method is CASH
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
        notes: String = ""
    ): Result<CheckoutReceipt> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Keranjang belanja masih kosong"))
        }

        database.withTransaction {
            // STEP 1: Verify Stock Sufficiency
            for (item in cartItems) {
                if (item.trackStock) {
                    val balance = inventoryDao.getStockBalance(businessId, item.productId, item.variantId)
                    val currentStock = balance?.quantity ?: 0
                    if (currentStock < item.quantity && !item.allowNegativeStock) {
                        val displayName = if (item.variantName != null) {
                            "${item.productName} (${item.variantName})"
                        } else {
                            item.productName
                        }
                        return@withTransaction Result.failure(
                            IllegalStateException("Stok tidak mencukupi untuk \"$displayName\". Sisa stok: $currentStock, diminta: ${item.quantity}.")
                        )
                    }
                }
            }

            // Calculations
            val rawSubtotal = cartItems.sumOf { it.subtotal }
            val totalAmount = (rawSubtotal - orderDiscount + taxAmount).coerceAtLeast(0L)

            if (paymentMethod == "CASH" && amountPaid < totalAmount) {
                return@withTransaction Result.failure(
                    IllegalArgumentException("Nominal pembayaran tunai kurang dari total belanja")
                )
            }

            val changeAmount = if (paymentMethod == "CASH") {
                (amountPaid - totalAmount).coerceAtLeast(0L)
            } else {
                0L
            }

            val saleId = UUID.randomUUID().toString()
            val invoiceNumber = generateInvoiceNumber(businessId)

            // STEP 2: Insert Sale
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
                paymentMethod = paymentMethod,
                paymentStatus = "PAID",
                notes = notes,
                createdAt = System.currentTimeMillis()
            )
            posDao.insertSale(sale)

            // STEP 3: Insert Sale Items
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

            // STEP 4: Insert Payment
            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                saleId = saleId,
                businessId = businessId,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                changeAmount = changeAmount,
                referenceNumber = if (paymentMethod == "BANK_TRANSFER" || paymentMethod == "QRIS") {
                    "REF-" + UUID.randomUUID().toString().take(8).uppercase()
                } else null,
                status = "SUCCESS",
                createdAt = System.currentTimeMillis()
            )
            posDao.insertPayment(payment)

            // STEP 5: Reduce Stock & Record Stock Movements
            for (item in cartItems) {
                if (item.trackStock) {
                    val currentBalanceEntity = inventoryDao.getStockBalance(businessId, item.productId, item.variantId)
                    val currentQty = currentBalanceEntity?.quantity ?: 0
                    val newQty = currentQty - item.quantity

                    val balanceId = currentBalanceEntity?.id ?: UUID.randomUUID().toString()
                    val updatedBalance = StockBalanceEntity(
                        id = balanceId,
                        businessId = businessId,
                        productId = item.productId,
                        variantId = item.variantId,
                        quantity = newQty,
                        updatedAt = System.currentTimeMillis()
                    )
                    inventoryDao.insertOrUpdateBalance(updatedBalance)

                    val stockMovement = StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        businessId = businessId,
                        productId = item.productId,
                        variantId = item.variantId,
                        type = "SALE",
                        quantity = -item.quantity,
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

            // STEP 6: Insert Cash Transaction if CASH payment
            val cashTx = if (paymentMethod == "CASH") {
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
                null
            }

            Result.success(
                CheckoutReceipt(
                    sale = sale,
                    items = saleItems,
                    payment = payment,
                    cashTransaction = cashTx
                )
            )
        }
    }
}
