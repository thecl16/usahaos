package com.example.data.repository

import com.example.data.local.dao.CustomerDao
import com.example.data.local.entity.CustomerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class CustomerRepository(
    private val customerDao: CustomerDao
) {
    fun getCustomersFlow(businessId: String): Flow<List<CustomerEntity>> {
        return customerDao.getCustomers(businessId)
    }

    suspend fun getCustomerById(id: String): CustomerEntity? = withContext(Dispatchers.IO) {
        customerDao.getCustomerById(id)
    }

    suspend fun createCustomer(
        businessId: String,
        name: String,
        phone: String? = null,
        email: String? = null,
        address: String? = null
    ): Result<CustomerEntity> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Nama pelanggan wajib diisi"))
        }

        val existing = customerDao.getCustomerByName(businessId, trimmedName)
        if (existing != null) {
            return@withContext Result.success(existing)
        }

        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            name = trimmedName,
            phone = phone?.trim()?.ifEmpty { null },
            email = email?.trim()?.ifEmpty { null },
            address = address?.trim()?.ifEmpty { null },
            createdAt = System.currentTimeMillis()
        )
        customerDao.insertCustomer(customer)
        Result.success(customer)
    }
}
