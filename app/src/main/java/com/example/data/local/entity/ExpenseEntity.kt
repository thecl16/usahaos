package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
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
        Index(value = ["businessId", "category"]),
        Index(value = ["createdAt"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val category: String,
    val description: String,
    val amount: Long,
    val paymentMethod: String, // CASH, BANK_TRANSFER, QRIS, OTHER
    val referenceNumber: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
