package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.CashDao
import com.example.data.local.dao.PayableDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.entity.PayableEntity
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.PurchaseItemEntity
import com.example.data.local.entity.StockBalanceEntity
import com.example.data.local.entity.StockMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

data class PurchaseItemInput(
    val productId: String,
    val variantId: String? = null,
    val productName: String,
    val variantName: String? = null,
    val quantity: Int,
    val unitCost: Long
)

class PurchaseRepository(
    private val database: AppDatabase,
    private val purchaseDao: PurchaseDao,
    private val payableDao: PayableDao,
    private val inventoryDao: InventoryDao,
    private val productDao: ProductDao,
    private val cashDao: CashDao
) {

    fun getAllFlow(
        businessId: String
    ): Flow<List<PurchaseEntity>> =
        purchaseDao.getAllFlow(businessId)

    fun getUnpaidFlow(
        businessId: String
    ): Flow<List<PurchaseEntity>> =
        purchaseDao.getUnpaidFlow(businessId)

    suspend fun getById(
        businessId: String,
        id: String
    ): PurchaseEntity? =
        purchaseDao.getById(businessId, id)

    suspend fun createPurchase(
        businessId: String,
        supplierId: String?,
        supplierName: String,
        invoiceNumber: String,
        items: List<PurchaseItemInput>,
        discountAmount: Long = 0L,
        paymentStatus: String = "UNPAID",
        paidAmount: Long = 0L,
        dueDate: Long? = null,
        notes: String = ""
    ): Result<PurchaseEntity> = withContext(Dispatchers.IO) {

        if (items.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("Item pembelian tidak boleh kosong")
            )
        }

        if (invoiceNumber.trim().isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Nomor invoice wajib diisi")
            )
        }

        if (discountAmount < 0L) {
            return@withContext Result.failure(
                IllegalArgumentException("Diskon tidak boleh negatif")
            )
        }

        if (paidAmount < 0L) {
            return@withContext Result.failure(
                IllegalArgumentException("Jumlah pembayaran tidak boleh negatif")
            )
        }

        items.forEach {
            if (it.quantity <= 0) {
                return@withContext Result.failure(
                    IllegalArgumentException("Jumlah item harus lebih besar dari 0")
                )
            }

            if (it.unitCost < 0L) {
                return@withContext Result.failure(
                    IllegalArgumentException("Harga modal tidak boleh negatif")
                )
            }
        }

        val existingInvoice = purchaseDao.getByInvoiceNumber(
            businessId,
            invoiceNumber.trim()
        )

        if (existingInvoice != null) {
            return@withContext Result.failure(
                IllegalArgumentException("Nomor invoice sudah digunakan")
            )
        }

        val subtotal = items.sumOf {
            it.quantity.toLong() * it.unitCost
        }

        val totalAmount = (subtotal - discountAmount).coerceAtLeast(0L)

        if (paidAmount > totalAmount) {
            return@withContext Result.failure(
                IllegalArgumentException("Pembayaran melebihi total pembelian")
            )
        }

        val normalizedStatus = when {
            paidAmount >= totalAmount && totalAmount > 0L -> "PAID"
            paidAmount > 0L -> "PARTIAL"
            else -> "UNPAID"
        }

        database.withTransaction {

            val purchaseId = UUID.randomUUID().toString()

            val purchase = PurchaseEntity(
                id = purchaseId,
                businessId = businessId,
                supplierId = supplierId,
                supplierName = supplierName.trim(),
                invoiceNumber = invoiceNumber.trim(),
                subtotal = subtotal,
                discountAmount = discountAmount,
                totalAmount = totalAmount,
                paymentStatus = normalizedStatus,
                paidAmount = paidAmount,
                notes = notes.trim()
            )

            purchaseDao.insertPurchase(purchase)

            val purchaseItems = items.map {
                PurchaseItemEntity(
                    id = UUID.randomUUID().toString(),
                    purchaseId = purchaseId,
                    businessId = businessId,
                    productId = it.productId,
                    variantId = it.variantId,
                    productName = it.productName,
                    variantName = it.variantName,
                    quantity = it.quantity,
                    unitCost = it.unitCost,
                    subtotal = it.quantity.toLong() * it.unitCost
                )
            }

            purchaseDao.insertItems(purchaseItems)

            if (paidAmount > 0L) {
                cashDao.insertCashTransaction(
                    CashTransactionEntity(
                        id = UUID.randomUUID().toString(),
                        businessId = businessId,
                        type = "OUT",
                        category = "PEMBELIAN",
                        amount = paidAmount,
                        referenceType = "PURCHASE",
                        referenceId = purchaseId,
                        notes = "Pembayaran pembelian ${invoiceNumber.trim()}",
                        createdAt = System.currentTimeMillis()
                    )
                )
            }

            for (item in items) {

                val product = productDao.getProductByIdForBusiness(
                    businessId,
                    item.productId
                ) ?: throw IllegalArgumentException(
                    "Produk tidak ditemukan: ${item.productName}"
                )

                val currentBalance =
                    inventoryDao.getStockBalance(
                        businessId,
                        item.productId,
                        item.variantId
                    )

                val currentQty = currentBalance?.quantity ?: 0
                val newQty = currentQty + item.quantity

                val balance = StockBalanceEntity(
                    id = currentBalance?.id ?: UUID.randomUUID().toString(),
                    businessId = businessId,
                    productId = product.id,
                    variantId = item.variantId,
                    quantity = newQty,
                    updatedAt = System.currentTimeMillis()
                )

                inventoryDao.insertOrUpdateBalance(balance)

                inventoryDao.insertStockMovement(
                    StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        businessId = businessId,
                        productId = product.id,
                        variantId = item.variantId,
                        type = "IN",
                        quantity = item.quantity,
                        balanceBefore = currentQty,
                        balanceAfter = newQty,
                        referenceType = "PURCHASE",
                        referenceId = purchaseId,
                        notes = "Pembelian ${invoiceNumber.trim()}",
                        createdAt = System.currentTimeMillis()
                    )
                )
            }

            if (normalizedStatus != "PAID") {

                val payableAmount = totalAmount - paidAmount

                if (payableAmount > 0L) {
                    payableDao.insert(
                        PayableEntity(
                            id = UUID.randomUUID().toString(),
                            businessId = businessId,
                            purchaseId = purchaseId,
                            supplierId = supplierId,
                            supplierName = supplierName.trim(),
                            amount = payableAmount,
                            paidAmount = 0L,
                            dueDate = dueDate,
                            status = "UNPAID",
                            notes = "Utang dari pembelian ${invoiceNumber.trim()}",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            Result.success(purchase)
        }
    }
}
