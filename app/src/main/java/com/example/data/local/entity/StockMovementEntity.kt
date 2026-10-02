package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
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
        Index(value = ["referenceId"]),
        Index(value = ["createdAt"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val productId: String,
    val variantId: String? = null,
    val type: String, // IN, OUT, ADJUSTMENT, OPNAME, SALE
    val quantity: Int, // Positive for IN, Negative for OUT/SALE, delta for ADJUSTMENT/OPNAME
    val balanceBefore: Int,
    val balanceAfter: Int,
    val referenceType: String, // MANUAL, OPNAME, SALE, INITIAL
    val referenceId: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
