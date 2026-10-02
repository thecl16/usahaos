package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
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
        Index(value = ["businessId", "prefix"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val prefix: String, // 2-5 uppercase chars e.g. BEV, FOD, SNK
    val businessType: String, // FNB, RETAIL, SERVICE
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
