package com.example.data.repository

import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SupplierRepository(
    private val supplierDao: SupplierDao
) {

    fun getAllFlow(businessId: String): Flow<List<SupplierEntity>> =
        supplierDao.getAllFlow(businessId)

    fun search(
        businessId: String,
        query: String
    ): Flow<List<SupplierEntity>> =
        supplierDao.search(businessId, query)

    suspend fun getById(
        businessId: String,
        id: String
    ): SupplierEntity? =
        supplierDao.getById(businessId, id)

    suspend fun create(
        businessId: String,
        name: String,
        phone: String? = null,
        email: String? = null,
        address: String? = null,
        notes: String? = null
    ): SupplierEntity {
        val supplier = SupplierEntity(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            name = name.trim(),
            phone = phone?.trim()?.ifBlank { null },
            email = email?.trim()?.ifBlank { null },
            address = address?.trim()?.ifBlank { null },
            notes = notes?.trim()?.ifBlank { null }
        )

        supplierDao.insert(supplier)
        return supplier
    }

    suspend fun update(supplier: SupplierEntity) {
        supplierDao.update(supplier)
    }

    suspend fun delete(supplier: SupplierEntity) {
        supplierDao.delete(supplier)
    }
}
