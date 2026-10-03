package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PayableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayableDao {

    @Query("""
        SELECT * FROM payables
        WHERE businessId = :businessId
        ORDER BY createdAt DESC
    """)
    fun getAllFlow(
        businessId: String
    ): Flow<List<PayableEntity>>

    @Query("""
        SELECT * FROM payables
        WHERE businessId = :businessId
        AND status != 'PAID'
        ORDER BY dueDate ASC, createdAt DESC
    """)
    fun getUnpaidFlow(
        businessId: String
    ): Flow<List<PayableEntity>>

    @Query("""
        SELECT * FROM payables
        WHERE businessId = :businessId
        AND id = :id
        LIMIT 1
    """)
    suspend fun getById(
        businessId: String,
        id: String
    ): PayableEntity?

    @Insert
    suspend fun insert(
        payable: PayableEntity
    )

    @Update
    suspend fun update(
        payable: PayableEntity
    )

    @Query("""
        DELETE FROM payables
        WHERE businessId = :businessId
        AND id = :id
    """)
    suspend fun delete(
        businessId: String,
        id: String
    )
}
