package com.example.data.repository

import com.example.data.local.dao.ActiveSessionDao
import com.example.data.local.dao.BusinessDao
import com.example.data.local.dao.BusinessUserDao
import com.example.data.local.dao.FeatureFlagDao
import com.example.data.local.entity.ActiveSessionEntity
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.BusinessUserEntity
import com.example.data.local.entity.FeatureFlagEntity
import com.example.domain.model.BusinessType
import com.example.domain.model.FeatureRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class BusinessRepository(
    private val businessDao: BusinessDao,
    private val businessUserDao: BusinessUserDao,
    private val featureFlagDao: FeatureFlagDao,
    private val activeSessionDao: ActiveSessionDao
) {
    fun getUserBusinessesFlow(userId: String): Flow<List<BusinessEntity>> {
        return businessUserDao.getBusinessesForUser(userId)
    }

    suspend fun getUserBusinessesList(userId: String): List<BusinessEntity> = withContext(Dispatchers.IO) {
        businessUserDao.getBusinessesForUserList(userId)
    }

    fun getBusinessByIdFlow(businessId: String): Flow<BusinessEntity?> {
        return businessDao.getBusinessByIdFlow(businessId)
    }

    suspend fun createBusiness(
        name: String,
        type: BusinessType,
        address: String,
        phone: String,
        email: String,
        currency: String = "IDR",
        userId: String
    ): Result<BusinessEntity> = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nama usaha tidak boleh kosong"))
        }

        val businessId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val business = BusinessEntity(
            id = businessId,
            name = cleanName,
            type = type.code,
            address = address.trim(),
            phone = phone.trim(),
            email = email.trim(),
            currency = currency.ifBlank { "IDR" },
            createdAt = now,
            updatedAt = now
        )

        // 1. Insert business
        businessDao.insertBusiness(business)

        // 2. Insert owner membership
        val membership = BusinessUserEntity(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            userId = userId,
            role = "OWNER",
            isActive = true,
            createdAt = now
        )
        businessUserDao.insertBusinessUser(membership)

        // 3. Initialize default feature flags for this business type
        val defaultFlags = FeatureRegistry.getDefaultFlagsFor(type).map { (key, isEnabled) ->
            FeatureFlagEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                featureKey = key,
                isEnabled = isEnabled,
                updatedAt = now
            )
        }
        featureFlagDao.insertAll(defaultFlags)

        // 4. Set as active business in session
        activeSessionDao.updateCurrentBusiness(businessId)

        Result.success(business)
    }

    suspend fun switchActiveBusiness(businessId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val session = activeSessionDao.getSession()
        val userId = session?.currentUserId
            ?: return@withContext Result.failure(IllegalStateException("Sesi pengguna tidak valid. Silakan login kembali."))

        val business = businessDao.getBusinessById(businessId)
            ?: return@withContext Result.failure(IllegalArgumentException("Usaha tidak ditemukan."))

        val membership = businessUserDao.getMembership(businessId, userId)
        if (membership == null || !membership.isActive) {
            return@withContext Result.failure(IllegalAccessException("Anda tidak memiliki akses aktif ke usaha ini."))
        }

        activeSessionDao.updateCurrentBusiness(businessId)
        Result.success(Unit)
    }

    suspend fun updateBusiness(business: BusinessEntity): Result<Unit> = withContext(Dispatchers.IO) {
        businessDao.updateBusiness(business.copy(updatedAt = System.currentTimeMillis()))
        Result.success(Unit)
    }
}
