package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.BusinessUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessUser(businessUser: BusinessUserEntity)

    @Query("""
        SELECT b.* FROM businesses b 
        INNER JOIN business_users bu ON b.id = bu.businessId 
        WHERE bu.userId = :userId AND bu.isActive = 1 
        ORDER BY b.createdAt DESC
    """)
    fun getBusinessesForUser(userId: String): Flow<List<BusinessEntity>>

    @Query("""
        SELECT b.* FROM businesses b 
        INNER JOIN business_users bu ON b.id = bu.businessId 
        WHERE bu.userId = :userId AND bu.isActive = 1 
        ORDER BY b.createdAt DESC
    """)
    suspend fun getBusinessesForUserList(userId: String): List<BusinessEntity>

    @Query("SELECT * FROM business_users WHERE businessId = :businessId AND userId = :userId LIMIT 1")
    suspend fun getMembership(businessId: String, userId: String): BusinessUserEntity?

    @Query("SELECT * FROM business_users WHERE businessId = :businessId AND isActive = 1")
    fun getMembersForBusiness(businessId: String): Flow<List<BusinessUserEntity>>
}
