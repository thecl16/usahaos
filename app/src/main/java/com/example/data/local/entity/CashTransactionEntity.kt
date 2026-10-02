package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cash_transactions",
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
        Index(value = ["referenceId"]),
        Index(value = ["createdAt"])
    ]
)
data class CashTransactionEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val type: String, // IN, OUT
    val category: String, // PENJUALAN, MODAL_AWAL, PENGELUARAN, LAINNYA
    val amount: Long,
    val referenceType: String? = null, // SALE, MANUAL
    val referenceId: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
