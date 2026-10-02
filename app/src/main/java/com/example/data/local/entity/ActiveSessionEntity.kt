package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @PrimaryKey val id: Int = 1,
    val currentUserId: String?,
    val currentBusinessId: String?,
    val lastActiveAt: Long = System.currentTimeMillis()
)
