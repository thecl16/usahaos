package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payables",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.SET_NULL
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
        Index(value = ["businessId", "purchaseId"]),
        Index(value = ["businessId", "status"]),
        Index(value = ["dueDate"])
    ]
)
data class PayableEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val purchaseId: String? = null,
    val supplierId: String? = null,
    val supplierName: String = "",
    val amount: Long,
    val paidAmount: Long = 0L,
    val dueDate: Long? = null,
    val status: String, // UNPAID, PARTIAL, PAID, OVERDUE
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
