package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_coach_sessions",
    indices = [Index("userId")]
)
data class AiCoachSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val userId: Int,
    val topic: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ai_coach_messages",
    indices = [Index("sessionId")]
)
data class AiCoachMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0,
    val sessionId: Int,
    val sender: String, // "USER" or "AI"
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
