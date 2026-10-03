package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CashDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class ExpenseRepository(
    private val database: AppDatabase,
    private val expenseDao: ExpenseDao,
    private val cashDao: CashDao
) {

    fun getAllFlow(
        businessId: String
    ): Flow<List<ExpenseEntity>> =
        expenseDao.getAllFlow(businessId)

    fun getByCategoryFlow(
        businessId: String,
        category: String
    ): Flow<List<ExpenseEntity>> =
        expenseDao.getByCategoryFlow(
            businessId = businessId,
            category = category
        )

    suspend fun getById(
        businessId: String,
        id: String
    ): ExpenseEntity? =
        expenseDao.getById(
            businessId = businessId,
            id = id
        )

    suspend fun create(
        businessId: String,
        category: String,
        description: String,
        amount: Long,
        paymentMethod: String,
        referenceNumber: String? = null,
        notes: String = ""
    ): Result<ExpenseEntity> = withContext(Dispatchers.IO) {

        if (category.trim().isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Kategori pengeluaran wajib diisi"
                )
            )
        }

        if (description.trim().isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Deskripsi pengeluaran wajib diisi"
                )
            )
        }

        if (amount <= 0L) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Jumlah pengeluaran harus lebih besar dari 0"
                )
            )
        }

        val normalizedPaymentMethod =
            paymentMethod.trim().uppercase()

        val allowedPaymentMethods = setOf(
            "CASH",
            "BANK_TRANSFER",
            "QRIS",
            "OTHER"
        )

        if (normalizedPaymentMethod !in allowedPaymentMethods) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Metode pembayaran tidak valid"
                )
            )
        }

        database.withTransaction {

            val expenseId = UUID.randomUUID().toString()

            val expense = ExpenseEntity(
                id = expenseId,
                businessId = businessId,
                category = category.trim(),
                description = description.trim(),
                amount = amount,
                paymentMethod = normalizedPaymentMethod,
                referenceNumber = referenceNumber
                    ?.trim()
                    ?.ifBlank { null },
                notes = notes.trim()
            )

            expenseDao.insert(expense)

            if (normalizedPaymentMethod == "CASH") {
                insertCashTransaction(expense)
            }

            Result.success(expense)
        }
    }

    suspend fun update(
        expense: ExpenseEntity
    ): Result<Unit> = withContext(Dispatchers.IO) {

        if (expense.category.trim().isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Kategori pengeluaran wajib diisi"
                )
            )
        }

        if (expense.description.trim().isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Deskripsi pengeluaran wajib diisi"
                )
            )
        }

        if (expense.amount <= 0L) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Jumlah pengeluaran harus lebih besar dari 0"
                )
            )
        }

        val normalizedPaymentMethod =
            expense.paymentMethod.trim().uppercase()

        val allowedPaymentMethods = setOf(
            "CASH",
            "BANK_TRANSFER",
            "QRIS",
            "OTHER"
        )

        if (normalizedPaymentMethod !in allowedPaymentMethods) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "Metode pembayaran tidak valid"
                )
            )
        }

        database.withTransaction {

            cashDao.deleteByReference(
                businessId = expense.businessId,
                referenceType = "EXPENSE",
                referenceId = expense.id
            )

            val updatedExpense = expense.copy(
                category = expense.category.trim(),
                description = expense.description.trim(),
                amount = expense.amount,
                paymentMethod = normalizedPaymentMethod,
                referenceNumber = expense.referenceNumber
                    ?.trim()
                    ?.ifBlank { null },
                notes = expense.notes.trim()
            )

            expenseDao.update(updatedExpense)

            if (normalizedPaymentMethod == "CASH") {
                insertCashTransaction(updatedExpense)
            }
        }

        Result.success(Unit)
    }

    suspend fun delete(
        expense: ExpenseEntity
    ) {
        database.withTransaction {

            cashDao.deleteByReference(
                businessId = expense.businessId,
                referenceType = "EXPENSE",
                referenceId = expense.id
            )

            expenseDao.delete(expense)
        }
    }

    private suspend fun insertCashTransaction(
        expense: ExpenseEntity
    ) {
        cashDao.insertCashTransaction(
            CashTransactionEntity(
                id = UUID.randomUUID().toString(),
                businessId = expense.businessId,
                type = "OUT",
                category = "PENGELUARAN",
                amount = expense.amount,
                referenceType = "EXPENSE",
                referenceId = expense.id,
                notes = expense.description,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
