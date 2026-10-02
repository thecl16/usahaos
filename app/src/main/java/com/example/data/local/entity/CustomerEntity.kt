package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
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
        Index(value = ["businessId", "name"])
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
