package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "challenges"
)
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val challengeId: Int = 0,
    val title: String,
    val description: String,
    val targetDistanceKm: Double,
    val rewardPoints: Int,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true
)

@Entity(
    tableName = "user_challenges",
    primaryKeys = ["userId", "challengeId"],
    indices = [Index("userId"), Index("challengeId")]
)
data class UserChallengeEntity(
    val userId: Int,
    val challengeId: Int,
    val currentProgressKm: Double = 0.0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
