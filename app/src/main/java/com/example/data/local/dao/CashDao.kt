package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CashTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCashTransaction(tx: CashTransactionEntity)

    @Query("SELECT * FROM cash_transactions WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getCashTransactions(businessId: String): Flow<List<CashTransactionEntity>>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'IN' THEN amount ELSE -amount END), 0) FROM cash_transactions WHERE businessId = :businessId")
    fun getCashBalanceFlow(businessId: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'IN' THEN amount ELSE -amount END), 0) FROM cash_transactions WHERE businessId = :businessId")
    suspend fun getCashBalanceSync(businessId: String): Long
}
