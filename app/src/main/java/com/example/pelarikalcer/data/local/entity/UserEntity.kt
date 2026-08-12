package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    
    // Profile Data
    val fullName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    
    // Physical Metrics
    val weightKg: Double = 0.0,
    val heightCm: Double? = null,
    val birthDate: Long? = null, // Stored as Unix timestamp
    val gender: String? = null,
    
    // Gamification Stats
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val lastRunDate: Long? = null, // Unix timestamp
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
