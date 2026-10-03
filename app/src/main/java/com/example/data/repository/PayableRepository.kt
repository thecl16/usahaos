package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CashDao
import com.example.data.local.dao.PayableDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.PayableEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class PayableRepository(
    private val database: AppDatabase,
    private val payableDao: PayableDao,
    private val cashDao: CashDao,
    private val purchaseDao: PurchaseDao = database.purchaseDao(),
    private val supplierDao: SupplierDao = database.supplierDao()
) {

    fun getAllFlow(
        businessId: String
    ): Flow<List<PayableEntity>> =
        payableDao.getAllFlow(businessId)

    fun getUnpaidFlow(
        businessId: String
    ): Flow<List<PayableEntity>> =
        payableDao.getUnpaidFlow(businessId)

    suspend fun getById(
        businessId: String,
        id: String
    ): PayableEntity? =
        payableDao.getById(
            businessId = businessId,
            id = id
        )

    suspend fun create(
        businessId: String,
        purchaseId: String?,
        supplierId: String?,
        supplierName: String,
        amount: Long,
        paidAmount: Long = 0L,
        dueDate: Long? = null,
        notes: String = ""
    ): Result<PayableEntity> = withContext(Dispatchers.IO) {

        if (amount < 0L) {
            return@withContext Result.failure(
                IllegalArgumentException("Jumlah utang tidak boleh negatif")
            )
        }

        if (paidAmount < 0L) {
            return@withContext Result.failure(
                IllegalArgumentException("Jumlah pembayaran tidak boleh negatif")
            )
        }

        if (paidAmount > amount) {
            return@withContext Result.failure(
                IllegalArgumentException("Pembayaran melebihi jumlah utang")
            )
        }

        if (supplierId != null) {
            val supplier = supplierDao.getById(businessId, supplierId)
            if (supplier == null) {
                return@withContext Result.failure(
                    IllegalArgumentException("Supplier tidak ditemukan atau bukan milik usaha aktif")
                )
            }
        }

        if (purchaseId != null) {
            val purchase = purchaseDao.getById(businessId, purchaseId)
            if (purchase == null) {
                return@withContext Result.failure(
                    IllegalArgumentException("Pembelian tidak ditemukan atau bukan milik usaha aktif")
                )
            }
        }

        val status = when {
            paidAmount >= amount && amount > 0L -> "PAID"
            paidAmount > 0L -> "PARTIAL"
            else -> "UNPAID"
        }

        val payable = PayableEntity(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            purchaseId = purchaseId,
            supplierId = supplierId,
            supplierName = supplierName.trim(),
            amount = amount,
            paidAmount = paidAmount,
            dueDate = dueDate,
            status = status,
            notes = notes.trim()
        )

        payableDao.insert(payable)

        Result.success(payable)
    }

    suspend fun update(
        payable: PayableEntity
    ): Result<Unit> {

        if (payable.amount < 0L) {
            return Result.failure(
                IllegalArgumentException("Jumlah utang tidak boleh negatif")
            )
        }

        if (payable.paidAmount < 0L) {
            return Result.failure(
                IllegalArgumentException("Jumlah pembayaran tidak boleh negatif")
            )
        }

        if (payable.paidAmount > payable.amount) {
            return Result.failure(
                IllegalArgumentException("Pembayaran melebihi jumlah utang")
            )
        }

        val updatedStatus = when {
            payable.paidAmount >= payable.amount &&
                payable.amount > 0L -> "PAID"

            payable.paidAmount > 0L -> "PARTIAL"

            else -> "UNPAID"
        }

        payableDao.update(
            payable.copy(
                status = updatedStatus
            )
        )

        return Result.success(Unit)
    }

    suspend fun pay(
        businessId: String,
        id: String,
        amount: Long,
        paymentMethod: String = "CASH"
    ): Result<PayableEntity> = withContext(Dispatchers.IO) {

        if (amount <= 0L) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Jumlah pembayaran harus lebih besar dari 0"
                )
            )
        }

        val normalizedPaymentMethod = paymentMethod
            .trim()
            .uppercase()

        val allowedMethods = setOf(
            "CASH",
            "BANK_TRANSFER",
            "QRIS",
            "OTHER"
        )

        if (normalizedPaymentMethod !in allowedMethods) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Metode pembayaran tidak valid"
                )
            )
        }

        runCatching {
            database.withTransaction {
                val payable = payableDao.getById(
                    businessId = businessId,
                    id = id
                ) ?: throw IllegalArgumentException(
                    "Utang tidak ditemukan atau bukan milik usaha aktif"
                )

                val remaining = payable.amount - payable.paidAmount

                if (remaining <= 0L) {
                    throw IllegalStateException(
                        "Utang sudah lunas"
                    )
                }

                if (amount > remaining) {
                    throw IllegalArgumentException(
                        "Pembayaran melebihi sisa utang"
                    )
                }

                // Sync related PurchaseEntity if this payable is linked to a purchase
                if (payable.purchaseId != null) {
                    val purchase = purchaseDao.getById(businessId, payable.purchaseId)
                    if (purchase != null) {
                        val newPurchasePaid = purchase.paidAmount + amount
                        if (newPurchasePaid > purchase.totalAmount) {
                            throw IllegalArgumentException("Pembayaran melebihi total tagihan pembelian")
                        }
                        val newPurchaseStatus = when {
                            newPurchasePaid >= purchase.totalAmount && purchase.totalAmount > 0L -> "PAID"
                            newPurchasePaid > 0L -> "PARTIAL"
                            else -> "UNPAID"
                        }
                        purchaseDao.updatePurchase(
                            purchase.copy(
                                paidAmount = newPurchasePaid,
                                paymentStatus = newPurchaseStatus
                            )
                        )
                    }
                }

                val newPaidAmount = payable.paidAmount + amount

                val newStatus = when {
                    newPaidAmount >= payable.amount -> "PAID"
                    newPaidAmount > 0L -> "PARTIAL"
                    else -> "UNPAID"
                }

                val updatedPayable = payable.copy(
                    paidAmount = newPaidAmount,
                    status = newStatus
                )

                payableDao.update(updatedPayable)

                // ONLY CASH payments create a CashTransaction OUT and reduce cash balance
                if (normalizedPaymentMethod == "CASH") {
                    cashDao.insertCashTransaction(
                        CashTransactionEntity(
                            id = UUID.randomUUID().toString(),
                            businessId = businessId,
                            type = "OUT",
                            category = "PEMBAYARAN UTANG",
                            amount = amount,
                            referenceType = "PAYABLE",
                            referenceId = payable.id,
                            notes = "Pembayaran utang ${payable.supplierName}",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                updatedPayable
            }
        }
    }

    suspend fun delete(
        businessId: String,
        id: String
    ) {
        database.withTransaction {
            cashDao.deleteByReference(
                businessId = businessId,
                referenceType = "PAYABLE",
                referenceId = id
            )

            payableDao.delete(
                businessId = businessId,
                id = id
            )
        }
    }
}
