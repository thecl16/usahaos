package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.StockBalanceEntity
import com.example.data.local.entity.StockMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class StockOpnameResult {
    data class Success(val difference: Int, val movement: StockMovementEntity) : StockOpnameResult()
    data class Error(val message: String) : StockOpnameResult()
}

class InventoryRepository(
    private val database: AppDatabase,
    private val inventoryDao: InventoryDao,
    private val productDao: ProductDao
) {
    fun getAllStockBalancesFlow(businessId: String): Flow<List<StockBalanceEntity>> {
        return inventoryDao.getAllStockBalancesForBusiness(businessId)
    }

    fun getStockBalanceFlow(businessId: String, productId: String, variantId: String? = null): Flow<StockBalanceEntity?> {
        return inventoryDao.getStockBalanceFlow(businessId, productId, variantId)
    }

    suspend fun getStockBalance(businessId: String, productId: String, variantId: String? = null): Int = withContext(Dispatchers.IO) {
        inventoryDao.getStockBalance(businessId, productId, variantId)?.quantity ?: 0
    }

    fun getMovementsFlow(businessId: String): Flow<List<StockMovementEntity>> {
        return inventoryDao.getMovementsForBusiness(businessId)
    }

    fun getMovementsForProductFlow(businessId: String, productId: String): Flow<List<StockMovementEntity>> {
        return inventoryDao.getMovementsForProduct(businessId, productId)
    }

    /**
     * Records Stock In (Stok Masuk).
     * Atomically increases stock balance and records a StockMovementEntity.
     */
    suspend fun recordStockIn(
        businessId: String,
        productId: String,
        variantId: String? = null,
        quantity: Int,
        notes: String = ""
    ): Result<StockMovementEntity> = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Jumlah stok masuk harus lebih besar dari 0"))
        }

        database.withTransaction {
            val currentBalanceEntity = inventoryDao.getStockBalance(businessId, productId, variantId)
            val currentQty = currentBalanceEntity?.quantity ?: 0
            val newQty = currentQty + quantity

            val balanceId = currentBalanceEntity?.id ?: UUID.randomUUID().toString()
            val updatedBalance = StockBalanceEntity(
                id = balanceId,
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                quantity = newQty,
                updatedAt = System.currentTimeMillis()
            )
            inventoryDao.insertOrUpdateBalance(updatedBalance)

            val movement = StockMovementEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                type = "IN",
                quantity = quantity,
                balanceBefore = currentQty,
                balanceAfter = newQty,
                referenceType = "MANUAL",
                referenceId = null,
                notes = notes.ifBlank { "Stok Masuk Manual" },
                createdAt = System.currentTimeMillis()
            )
            inventoryDao.insertStockMovement(movement)
            Result.success(movement)
        }
    }

    /**
     * Records Stock Out (Stok Keluar).
     * Atomically decreases stock balance (validating negative stock) and records a StockMovementEntity.
     */
    suspend fun recordStockOut(
        businessId: String,
        productId: String,
        variantId: String? = null,
        quantity: Int,
        notes: String = ""
    ): Result<StockMovementEntity> = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Jumlah stok keluar harus lebih besar dari 0"))
        }

        val product = productDao.getProductByIdForBusiness(businessId, productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Produk tidak ditemukan"))

        database.withTransaction {
            val currentBalanceEntity = inventoryDao.getStockBalance(businessId, productId, variantId)
            val currentQty = currentBalanceEntity?.quantity ?: 0
            val newQty = currentQty - quantity

            if (newQty < 0 && !product.allowNegativeStock) {
                return@withTransaction Result.failure(
                    IllegalStateException("Stok tidak mencukupi untuk dikeluarkan. Sisa stok: $currentQty, Diminta: $quantity")
                )
            }

            val balanceId = currentBalanceEntity?.id ?: UUID.randomUUID().toString()
            val updatedBalance = StockBalanceEntity(
                id = balanceId,
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                quantity = newQty,
                updatedAt = System.currentTimeMillis()
            )
            inventoryDao.insertOrUpdateBalance(updatedBalance)

            val movement = StockMovementEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                type = "OUT",
                quantity = -quantity,
                balanceBefore = currentQty,
                balanceAfter = newQty,
                referenceType = "MANUAL",
                referenceId = null,
                notes = notes.ifBlank { "Stok Keluar Manual" },
                createdAt = System.currentTimeMillis()
            )
            inventoryDao.insertStockMovement(movement)
            Result.success(movement)
        }
    }

    /**
     * Records Stock Adjustment (Penyesuaian Stok).
     * Directly adjusts stock to new balance and records difference.
     */
    suspend fun recordStockAdjustment(
        businessId: String,
        productId: String,
        variantId: String? = null,
        newQuantity: Int,
        notes: String = ""
    ): Result<StockMovementEntity> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByIdForBusiness(businessId, productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Produk tidak ditemukan"))

        if (newQuantity < 0 && !product.allowNegativeStock) {
            return@withContext Result.failure(IllegalArgumentException("Stok tidak boleh minus"))
        }

        database.withTransaction {
            val currentBalanceEntity = inventoryDao.getStockBalance(businessId, productId, variantId)
            val currentQty = currentBalanceEntity?.quantity ?: 0
            val difference = newQuantity - currentQty

            val balanceId = currentBalanceEntity?.id ?: UUID.randomUUID().toString()
            val updatedBalance = StockBalanceEntity(
                id = balanceId,
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                quantity = newQuantity,
                updatedAt = System.currentTimeMillis()
            )
            inventoryDao.insertOrUpdateBalance(updatedBalance)

            val movement = StockMovementEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                type = "ADJUSTMENT",
                quantity = difference,
                balanceBefore = currentQty,
                balanceAfter = newQuantity,
                referenceType = "MANUAL",
                referenceId = null,
                notes = notes.ifBlank { "Penyesuaian Stok Manual" },
                createdAt = System.currentTimeMillis()
            )
            inventoryDao.insertStockMovement(movement)
            Result.success(movement)
        }
    }

    /**
     * Records Stock Opname:
     * Expected Stock → Physical Stock → Difference → Confirm → Adjustment.
     * Never change stock silently.
     */
    suspend fun recordStockOpname(
        businessId: String,
        productId: String,
        variantId: String? = null,
        physicalStock: Int,
        notes: String = ""
    ): Result<StockMovementEntity> = withContext(Dispatchers.IO) {
        val product = productDao.getProductByIdForBusiness(businessId, productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Produk tidak ditemukan"))

        if (physicalStock < 0 && !product.allowNegativeStock) {
            return@withContext Result.failure(IllegalArgumentException("Stok fisik tidak boleh bernilai negatif"))
        }

        database.withTransaction {
            val currentBalanceEntity = inventoryDao.getStockBalance(businessId, productId, variantId)
            val expectedStock = currentBalanceEntity?.quantity ?: 0
            val difference = physicalStock - expectedStock

            val balanceId = currentBalanceEntity?.id ?: UUID.randomUUID().toString()
            val updatedBalance = StockBalanceEntity(
                id = balanceId,
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                quantity = physicalStock,
                updatedAt = System.currentTimeMillis()
            )
            inventoryDao.insertOrUpdateBalance(updatedBalance)

            val diffLabel = if (difference > 0) "+$difference" else "$difference"
            val opnameNote = "Stock Opname: Tercatat $expectedStock, Fisik $physicalStock (Selisih: $diffLabel). ${notes.trim()}"

            val movement = StockMovementEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                productId = productId,
                variantId = variantId,
                type = "OPNAME",
                quantity = difference,
                balanceBefore = expectedStock,
                balanceAfter = physicalStock,
                referenceType = "OPNAME",
                referenceId = null,
                notes = opnameNote,
                createdAt = System.currentTimeMillis()
            )
            inventoryDao.insertStockMovement(movement)
            Result.success(movement)
        }
    }
}
