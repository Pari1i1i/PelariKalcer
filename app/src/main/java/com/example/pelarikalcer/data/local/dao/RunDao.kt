package com.example.pelarikalcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pelarikalcer.data.local.entity.RunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity): Long

    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRunsByUser(userId: Int): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastRun(userId: Int): RunEntity?

    @Query("SELECT SUM(distanceKm) FROM runs WHERE userId = :userId")
    fun getTotalDistanceKm(userId: Int): Flow<Double?>

    @Query("SELECT SUM(caloriesBurned) FROM runs WHERE userId = :userId")
    fun getTotalCalories(userId: Int): Flow<Int?>

    @Query("SELECT COUNT(*) FROM runs WHERE userId = :userId")
    fun getTotalRunCount(userId: Int): Flow<Int>

    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY createdAt DESC LIMIT 5")
    fun getRecentRuns(userId: Int): Flow<List<RunEntity>>
}
