package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProductVariantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductVariantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVariant(variant: ProductVariantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Update
    suspend fun updateVariant(variant: ProductVariantEntity)

    @Query("SELECT * FROM product_variants WHERE productId = :productId ORDER BY createdAt ASC")
    fun getVariantsForProduct(productId: String): Flow<List<ProductVariantEntity>>

    @Query("SELECT * FROM product_variants WHERE productId = :productId ORDER BY createdAt ASC")
    suspend fun getVariantsForProductList(productId: String): List<ProductVariantEntity>

    @Query("SELECT * FROM product_variants WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getVariantsForBusiness(businessId: String): Flow<List<ProductVariantEntity>>

    @Query("SELECT * FROM product_variants WHERE businessId = :businessId AND UPPER(sku) = UPPER(:sku) LIMIT 1")
    suspend fun getVariantBySku(businessId: String, sku: String): ProductVariantEntity?

    @Query("SELECT * FROM product_variants WHERE businessId = :businessId AND barcode = :barcode LIMIT 1")
    suspend fun getVariantByBarcode(businessId: String, barcode: String): ProductVariantEntity?

    @Query("UPDATE product_variants SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateVariantStatus(id: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM product_variants WHERE id = :id")
    suspend fun deleteVariant(id: String)

    @Query("DELETE FROM product_variants WHERE productId = :productId")
    suspend fun deleteVariantsByProduct(productId: String)
}
