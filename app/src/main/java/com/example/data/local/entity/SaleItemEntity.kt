package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["businessId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val businessId: String,
    val productId: String,
    val variantId: String? = null,
    val productName: String,
    val variantName: String? = null,
    val unitPrice: Long,
    val quantity: Int,
    val discountAmount: Long = 0L,
    val subtotal: Long
)
