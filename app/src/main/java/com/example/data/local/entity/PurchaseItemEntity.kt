package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["purchaseId"]),
        Index(value = ["businessId", "productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey val id: String,
    val purchaseId: String,
    val businessId: String,
    val productId: String,
    val variantId: String? = null,
    val productName: String,
    val variantName: String? = null,
    val quantity: Int,
    val unitCost: Long,
    val subtotal: Long
)
