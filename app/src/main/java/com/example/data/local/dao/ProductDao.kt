package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getProductsForBusiness(businessId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND isActive = 1 ORDER BY productName ASC")
    fun getActiveProductsForBusiness(businessId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductByIdFlow(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND productCode = :code LIMIT 1")
    suspend fun getProductByCode(businessId: String, code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE businessId = :businessId AND UPPER(sku) = UPPER(:sku) LIMIT 1")
    suspend fun getProductBySku(businessId: String, sku: String): ProductEntity?

    @Query("SELECT * FROM products WHERE businessId = :businessId AND barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(businessId: String, barcode: String): ProductEntity?

    @Query("SELECT productCode FROM products WHERE businessId = :businessId AND productCode LIKE :prefixPattern")
    suspend fun getProductCodesStartingWith(businessId: String, prefixPattern: String): List<String>

    @Query("UPDATE products SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProductStatus(id: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)
}
