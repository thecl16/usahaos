package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feature_flags",
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
        Index(value = ["businessId", "featureKey"], unique = true)
    ]
)
data class FeatureFlagEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val featureKey: String,
    val isEnabled: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
