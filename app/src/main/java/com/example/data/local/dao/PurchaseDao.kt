package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.PurchaseItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Query("""
        SELECT * FROM purchases
        WHERE businessId = :businessId
        ORDER BY createdAt DESC
    """)
    fun getAllFlow(
        businessId: String
    ): Flow<List<PurchaseEntity>>

    @Query("""
        SELECT * FROM purchases
        WHERE businessId = :businessId
        AND id = :id
        LIMIT 1
    """)
    suspend fun getById(
        businessId: String,
        id: String
    ): PurchaseEntity?

    @Query("""
        SELECT * FROM purchases
        WHERE businessId = :businessId
        AND paymentStatus != 'PAID'
        ORDER BY createdAt DESC
    """)
    fun getUnpaidFlow(
        businessId: String
    ): Flow<List<PurchaseEntity>>

    @Query("""
        SELECT * FROM purchases
        WHERE businessId = :businessId
        AND invoiceNumber = :invoiceNumber
        LIMIT 1
    """)
    suspend fun getByInvoiceNumber(
        businessId: String,
        invoiceNumber: String
    ): PurchaseEntity?

    @Query("""
        SELECT * FROM purchase_items
        WHERE businessId = :businessId
        AND purchaseId = :purchaseId
        ORDER BY id ASC
    """)
    suspend fun getItems(
        businessId: String,
        purchaseId: String
    ): List<PurchaseItemEntity>

    @Insert
    suspend fun insertPurchase(
        purchase: PurchaseEntity
    )

    @Update
    suspend fun updatePurchase(
        purchase: PurchaseEntity
    )

    @Insert
    suspend fun insertItems(
        items: List<PurchaseItemEntity>
    )

    @Query("""
        DELETE FROM purchase_items
        WHERE businessId = :businessId
        AND purchaseId = :purchaseId
    """)
    suspend fun deleteItems(
        businessId: String,
        purchaseId: String
    )

    @Query("""
        DELETE FROM purchases
        WHERE businessId = :businessId
        AND id = :id
    """)
    suspend fun deletePurchase(
        businessId: String,
        id: String
    )
}
