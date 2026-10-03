package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query("""
        SELECT * FROM suppliers
        WHERE businessId = :businessId
        ORDER BY name ASC
    """)
    fun getAllFlow(businessId: String): Flow<List<SupplierEntity>>

    @Query("""
        SELECT * FROM suppliers
        WHERE businessId = :businessId
        AND id = :id
        LIMIT 1
    """)
    suspend fun getById(
        businessId: String,
        id: String
    ): SupplierEntity?

    @Query("""
        SELECT * FROM suppliers
        WHERE businessId = :businessId
        AND (
            name LIKE '%' || :query || '%'
            OR phone LIKE '%' || :query || '%'
            OR email LIKE '%' || :query || '%'
        )
        ORDER BY name ASC
    """)
    fun search(
        businessId: String,
        query: String
    ): Flow<List<SupplierEntity>>

    @Insert
    suspend fun insert(supplier: SupplierEntity)

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Delete
    suspend fun delete(supplier: SupplierEntity)
}
