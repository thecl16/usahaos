package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("""
        SELECT * FROM expenses
        WHERE businessId = :businessId
        ORDER BY createdAt DESC
    """)
    fun getAllFlow(
        businessId: String
    ): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses
        WHERE businessId = :businessId
        AND id = :id
        LIMIT 1
    """)
    suspend fun getById(
        businessId: String,
        id: String
    ): ExpenseEntity?

    @Query("""
        SELECT * FROM expenses
        WHERE businessId = :businessId
        AND category = :category
        ORDER BY createdAt DESC
    """)
    fun getByCategoryFlow(
        businessId: String,
        category: String
    ): Flow<List<ExpenseEntity>>

    @Insert
    suspend fun insert(
        expense: ExpenseEntity
    )

    @Update
    suspend fun update(
        expense: ExpenseEntity
    )

    @Delete
    suspend fun delete(
        expense: ExpenseEntity
    )
}
