package com.example.pelarikalcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pelarikalcer.data.local.entity.UserChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    // Versi Flow, buat observe reaktif di UI kalau nanti dibutuhkan
    @Query("SELECT challengeId FROM user_challenges WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedChallengeIds(userId: Int): Flow<List<Int>>

    // Versi snapshot (one-shot), dipakai buat cek cepat di MainViewModel
    // tiap kali totalDistanceKm berubah, tanpa perlu collect Flow terus-menerus.
    @Query("SELECT challengeId FROM user_challenges WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedChallengeIdsSnapshot(userId: Int): List<Int>

    @Query("SELECT * FROM user_challenges WHERE userId = :userId")
    fun getUserChallenges(userId: Int): Flow<List<UserChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserChallenge(entity: UserChallengeEntity)
}