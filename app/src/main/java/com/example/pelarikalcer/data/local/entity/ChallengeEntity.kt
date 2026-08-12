package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "challenges",
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

// challengeId di sini merujuk ke ID statis dari defaultChallenges (ChallengeCatalog.kt),
// BUKAN row di tabel "challenges" di atas — makanya sengaja gak ada foreign key
// ke ChallengeEntity. Tabel "challenges" itu buat fitur lain yang belum dipakai.
@Entity(
    tableName = "user_challenges",
    primaryKeys = ["userId", "challengeId"],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("challengeId")]
)
data class UserChallengeEntity(
    val userId: Int,
    val challengeId: Int,
    val currentProgressKm: Double = 0.0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)