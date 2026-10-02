package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "business_users",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["userId"]),
        Index(value = ["businessId", "userId"], unique = true)
    ]
)
data class BusinessUserEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val userId: String,
    val role: String = "OWNER", // OWNER, ADMIN, CASHIER, STAFF
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
