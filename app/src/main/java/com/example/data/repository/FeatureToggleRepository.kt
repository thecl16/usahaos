package com.example.data.repository

import com.example.data.local.dao.FeatureFlagDao
import com.example.data.local.entity.FeatureFlagEntity
import com.example.domain.model.BusinessType
import com.example.domain.model.FeatureRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class FeatureToggleRepository(
    private val featureFlagDao: FeatureFlagDao
) {
    fun getFeatureFlagsFlow(businessId: String): Flow<List<FeatureFlagEntity>> {
        return featureFlagDao.getFeatureFlagsFlow(businessId)
    }

    suspend fun toggleFeature(businessId: String, featureKey: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        val existing = featureFlagDao.isFeatureEnabled(businessId, featureKey)
        if (existing == null) {
            featureFlagDao.upsert(
                FeatureFlagEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = businessId,
                    featureKey = featureKey,
                    isEnabled = isEnabled
                )
            )
        } else {
            featureFlagDao.updateFlag(businessId, featureKey, isEnabled)
        }
    }

    suspend fun resetToDefaults(businessId: String, type: BusinessType) = withContext(Dispatchers.IO) {
        val defaultFlags = FeatureRegistry.getDefaultFlagsFor(type).map { (key, isEnabled) ->
            FeatureFlagEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                featureKey = key,
                isEnabled = isEnabled
            )
        }
        featureFlagDao.insertAll(defaultFlags)
    }
}
