package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
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
        Index(value = ["saleId"]),
        Index(value = ["businessId"])
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val businessId: String,
    val paymentMethod: String, // CASH, BANK_TRANSFER, QRIS, OTHER
    val amountPaid: Long,
    val changeAmount: Long = 0L,
    val referenceNumber: String? = null,
    val status: String = "SUCCESS",
    val createdAt: Long = System.currentTimeMillis()
)
