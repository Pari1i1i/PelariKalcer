package com.example.pelarikalcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pelarikalcer.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET totalPoints = totalPoints + :points WHERE userId = :userId")
    suspend fun addPoints(userId: Int, points: Int)

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByIdSnapshot(userId: Int): UserEntity?

    // Leaderboard
    @Query("SELECT * FROM users ORDER BY totalPoints DESC LIMIT 50")
    fun getLeaderboard(): kotlinx.coroutines.flow.Flow<List<UserEntity>>

    // Streak update helpers
    @Query("UPDATE users SET currentStreak = :streak, highestStreak = :highest, lastRunDate = :lastRunDate, updatedAt = :now WHERE userId = :userId")
    suspend fun updateStreak(userId: Int, streak: Int, highest: Int, lastRunDate: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE users SET totalPoints = totalPoints + :pts, updatedAt = :now WHERE userId = :userId")
    suspend fun addPoints(userId: Int, pts: Int, now: Long = System.currentTimeMillis())

    // Friends Features
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFriend(friend: com.example.pelarikalcer.data.local.entity.FriendEntity)

    @Query("DELETE FROM friends WHERE (userId = :userId AND friendUserId = :friendUserId) OR (userId = :friendUserId AND friendUserId = :userId)")
    suspend fun removeFriend(userId: Int, friendUserId: Int)

    @Query("""
        SELECT u.* FROM users u 
        INNER JOIN friends f ON u.userId = f.friendUserId 
        WHERE f.userId = :userId
    """)
    fun getFriendsList(userId: Int): Flow<List<UserEntity>>

    @Query("""
        SELECT * FROM users 
        WHERE userId = :userId OR userId IN (SELECT friendUserId FROM friends WHERE userId = :userId)
        ORDER BY totalPoints DESC
    """)
    fun getFriendsLeaderboard(userId: Int): Flow<List<UserEntity>>

    @Query("SELECT friendUserId FROM friends WHERE userId = :userId")
    fun getFriendUserIds(userId: Int): Flow<List<Int>>

    @Query("SELECT * FROM users WHERE userId != :userId AND userId NOT IN (SELECT friendUserId FROM friends WHERE userId = :userId) ORDER BY totalPoints DESC LIMIT 20")
    fun getSuggestedFriends(userId: Int): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE (username LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%') AND userId != :userId LIMIT 20")
    suspend fun searchUsers(query: String, userId: Int): List<UserEntity>
}
