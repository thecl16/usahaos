package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.entity.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun getCategoriesFlow(businessId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesForBusiness(businessId)
    }

    fun getActiveCategoriesFlow(businessId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getActiveCategoriesForBusiness(businessId)
    }

    suspend fun createCategory(
        businessId: String,
        name: String,
        prefix: String,
        businessType: String,
        description: String = ""
    ): Result<CategoryEntity> = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        val cleanPrefix = prefix.trim().uppercase()

        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nama kategori wajib diisi"))
        }

        // Validate prefix format: 2-5 uppercase alphanumeric chars
        val prefixRegex = Regex("^[A-Z0-9]{2,5}$")
        if (!prefixRegex.matches(cleanPrefix)) {
            return@withContext Result.failure(
                IllegalArgumentException("Prefix kode produk harus terdiri dari 2-5 karakter huruf kapital (contoh: BEV, FOD, SNK)")
            )
        }

        // Check duplicate prefix within business
        val existingWithPrefix = categoryDao.getCategoryByPrefix(businessId, cleanPrefix)
        if (existingWithPrefix != null) {
            return@withContext Result.failure(
                IllegalStateException("Prefix '$cleanPrefix' sudah digunakan oleh kategori '${existingWithPrefix.name}'. Harap gunakan prefix lain.")
            )
        }

        val category = CategoryEntity(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            name = cleanName,
            prefix = cleanPrefix,
            businessType = businessType,
            description = description.trim(),
            isActive = true
        )

        categoryDao.insertCategory(category)
        Result.success(category)
    }

    suspend fun updateCategory(
        id: String,
        businessId: String,
        name: String,
        prefix: String,
        businessType: String,
        description: String,
        isActive: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        val cleanPrefix = prefix.trim().uppercase()

        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nama kategori wajib diisi"))
        }

        val prefixRegex = Regex("^[A-Z0-9]{2,5}$")
        if (!prefixRegex.matches(cleanPrefix)) {
            return@withContext Result.failure(
                IllegalArgumentException("Prefix harus 2-5 karakter kapital")
            )
        }

        val existingWithPrefix = categoryDao.getCategoryByPrefix(businessId, cleanPrefix)
        if (existingWithPrefix != null && existingWithPrefix.id != id) {
            return@withContext Result.failure(
                IllegalStateException("Prefix '$cleanPrefix' sudah dipakai kategori lain")
            )
        }

        val existing = categoryDao.getCategoryById(id)
            ?: return@withContext Result.failure(IllegalArgumentException("Kategori tidak ditemukan"))

        val updated = existing.copy(
            name = cleanName,
            prefix = cleanPrefix,
            businessType = businessType,
            description = description.trim(),
            isActive = isActive,
            updatedAt = System.currentTimeMillis()
        )

        categoryDao.updateCategory(updated)
        Result.success(Unit)
    }

    suspend fun toggleCategoryStatus(id: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        categoryDao.updateCategoryStatus(id, isActive)
        Result.success(Unit)
    }
}
