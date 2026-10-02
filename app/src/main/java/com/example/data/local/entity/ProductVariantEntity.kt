package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_variants",
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
        Index(value = ["businessId", "sku"]),
        Index(value = ["businessId", "barcode"])
    ]
)
data class ProductVariantEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val productId: String,
    val variantName: String,
    val sku: String? = null,
    val barcode: String? = null,
    val purchasePrice: Long = 0L,
    val sellingPrice: Long = 0L,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
