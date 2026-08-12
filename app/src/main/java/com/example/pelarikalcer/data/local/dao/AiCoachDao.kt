package com.example.pelarikalcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pelarikalcer.data.local.entity.AiCoachMessageEntity
import com.example.pelarikalcer.data.local.entity.AiCoachSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiCoachDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AiCoachSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiCoachMessageEntity): Long

    @Query("SELECT * FROM ai_coach_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getMessages(sessionId: Int): Flow<List<AiCoachMessageEntity>>

    @Query("SELECT * FROM ai_coach_sessions WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestSession(userId: Int): AiCoachSessionEntity?

    @Query("SELECT * FROM ai_coach_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    suspend fun getMessagesSnapshot(sessionId: Int): List<AiCoachMessageEntity>
}
