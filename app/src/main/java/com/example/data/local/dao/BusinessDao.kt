package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getBusinessById(id: String): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    fun getBusinessByIdFlow(id: String): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses ORDER BY createdAt DESC")
    fun getAllBusinessesFlow(): Flow<List<BusinessEntity>>
}
