package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_balances",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["productId"]),
        Index(value = ["businessId", "productId", "variantId"], unique = true)
    ]
)
data class StockBalanceEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val productId: String,
    val variantId: String? = null,
    val quantity: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
