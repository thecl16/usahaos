package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
