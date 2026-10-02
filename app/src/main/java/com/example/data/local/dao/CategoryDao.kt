package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE businessId = :businessId ORDER BY name ASC")
    fun getCategoriesForBusiness(businessId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE businessId = :businessId AND isActive = 1 ORDER BY name ASC")
    fun getActiveCategoriesForBusiness(businessId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE businessId = :businessId")
    suspend fun getCategoriesForBusinessList(businessId: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE businessId = :businessId AND UPPER(prefix) = UPPER(:prefix) LIMIT 1")
    suspend fun getCategoryByPrefix(businessId: String, prefix: String): CategoryEntity?

    @Query("UPDATE categories SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCategoryStatus(id: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
}
