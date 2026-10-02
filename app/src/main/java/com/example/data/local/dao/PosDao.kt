package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("SELECT * FROM sales WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getSalesForBusiness(businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: String): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: String): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItemsFlow(saleId: String): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM payments WHERE saleId = :saleId LIMIT 1")
    suspend fun getPaymentForSale(saleId: String): PaymentEntity?

    @Query("SELECT COUNT(*) FROM sales WHERE businessId = :businessId AND createdAt >= :startOfDay AND createdAt <= :endOfDay")
    suspend fun getSalesCountToday(businessId: String, startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT SUM(totalAmount) FROM sales WHERE businessId = :businessId AND createdAt >= :startOfDay AND createdAt <= :endOfDay")
    suspend fun getTotalSalesToday(businessId: String, startOfDay: Long, endOfDay: Long): Long?

    @Query("SELECT invoiceNumber FROM sales WHERE businessId = :businessId AND invoiceNumber LIKE :datePattern ORDER BY invoiceNumber DESC LIMIT 1")
    suspend fun getLastInvoiceNumberToday(businessId: String, datePattern: String): String?
}
