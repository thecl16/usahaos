package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["categoryId"]),
        Index(value = ["businessId", "productCode"], unique = true),
        Index(value = ["businessId", "sku"]),
        Index(value = ["businessId", "barcode"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val categoryId: String,
    val productCode: String, // Read-only, auto-generated PREFIX + 8 digits
    val productName: String,
    val sku: String? = null,
    val barcode: String? = null,
    val unit: String = "pcs",
    val purchasePrice: Long = 0L,
    val sellingPrice: Long = 0L,
    val minStock: Int = 0,
    val trackStock: Boolean = true,
    val allowNegativeStock: Boolean = false,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
