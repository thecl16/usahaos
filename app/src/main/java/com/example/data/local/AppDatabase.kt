package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ActiveSessionDao
import com.example.data.local.dao.BusinessDao
import com.example.data.local.dao.BusinessUserDao
import com.example.data.local.dao.CashDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.FeatureFlagDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.PosDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ProductVariantDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ActiveSessionEntity
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.BusinessUserEntity
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.FeatureFlagEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockBalanceEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        BusinessEntity::class,
        BusinessUserEntity::class,
        FeatureFlagEntity::class,
        ActiveSessionEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        ProductVariantEntity::class,
        CustomerEntity::class,
        StockBalanceEntity::class,
        StockMovementEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentEntity::class,
        CashTransactionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun businessDao(): BusinessDao
    abstract fun businessUserDao(): BusinessUserDao
    abstract fun featureFlagDao(): FeatureFlagDao
    abstract fun activeSessionDao(): ActiveSessionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun productVariantDao(): ProductVariantDao
    abstract fun customerDao(): CustomerDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun posDao(): PosDao
    abstract fun cashDao(): CashDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "usahaos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
