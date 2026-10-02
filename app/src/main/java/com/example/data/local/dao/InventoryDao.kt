package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StockBalanceEntity
import com.example.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBalance(balance: StockBalanceEntity)

    @Query("SELECT * FROM stock_balances WHERE businessId = :businessId AND productId = :productId AND ((variantId IS NULL AND :variantId IS NULL) OR variantId = :variantId) LIMIT 1")
    suspend fun getStockBalance(businessId: String, productId: String, variantId: String?): StockBalanceEntity?

    @Query("SELECT * FROM stock_balances WHERE businessId = :businessId AND productId = :productId AND ((variantId IS NULL AND :variantId IS NULL) OR variantId = :variantId) LIMIT 1")
    fun getStockBalanceFlow(businessId: String, productId: String, variantId: String?): Flow<StockBalanceEntity?>

    @Query("SELECT * FROM stock_balances WHERE businessId = :businessId")
    fun getAllStockBalancesForBusiness(businessId: String): Flow<List<StockBalanceEntity>>

    @Query("SELECT * FROM stock_balances WHERE businessId = :businessId AND productId = :productId")
    fun getBalancesForProduct(businessId: String, productId: String): Flow<List<StockBalanceEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStockMovement(movement: StockMovementEntity)

    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getMovementsForBusiness(businessId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId AND productId = :productId ORDER BY createdAt DESC")
    fun getMovementsForProduct(businessId: String, productId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId AND type = :type ORDER BY createdAt DESC")
    fun getMovementsByType(businessId: String, type: String): Flow<List<StockMovementEntity>>
}
