package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "invoiceNumber"], unique = true),
        Index(value = ["createdAt"])
    ]
)
data class SaleEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val invoiceNumber: String,
    val customerId: String? = null,
    val customerName: String = "Pelanggan Umum",
    val subtotal: Long,
    val discountAmount: Long = 0L,
    val taxAmount: Long = 0L,
    val totalAmount: Long,
    val paymentMethod: String, // CASH, BANK_TRANSFER, QRIS, OTHER
    val paymentStatus: String = "PAID",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
