package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FeatureFlagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeatureFlagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flags: List<FeatureFlagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(flag: FeatureFlagEntity)

    @Query("SELECT * FROM feature_flags WHERE businessId = :businessId")
    fun getFeatureFlagsFlow(businessId: String): Flow<List<FeatureFlagEntity>>

    @Query("SELECT * FROM feature_flags WHERE businessId = :businessId")
    suspend fun getFeatureFlagsList(businessId: String): List<FeatureFlagEntity>

    @Query("SELECT isEnabled FROM feature_flags WHERE businessId = :businessId AND featureKey = :key LIMIT 1")
    suspend fun isFeatureEnabled(businessId: String, key: String): Boolean?

    @Query("UPDATE feature_flags SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE businessId = :businessId AND featureKey = :key")
    suspend fun updateFlag(businessId: String, key: String, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())
}
