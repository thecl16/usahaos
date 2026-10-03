package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "supplierId"]),
        Index(value = ["businessId", "invoiceNumber"]),
        Index(value = ["createdAt"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val supplierId: String? = null,
    val supplierName: String = "",
    val invoiceNumber: String,
    val subtotal: Long,
    val discountAmount: Long = 0L,
    val totalAmount: Long,
    val paymentStatus: String, // PAID, PARTIAL, UNPAID
    val paidAmount: Long = 0L,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
