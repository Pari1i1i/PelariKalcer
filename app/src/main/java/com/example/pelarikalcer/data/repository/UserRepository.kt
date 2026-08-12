package com.example.pelarikalcer.data.repository

import com.example.pelarikalcer.data.local.dao.UserDao
import com.example.pelarikalcer.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun register(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun login(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    fun getUser(userId: Int): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    suspend fun prepopulateMockUsers() {
        val mockUsers = listOf(
            UserEntity(username = "BudiSantoso", email = "budi@run.com", passwordHash = "budi123", fullName = "Budi Santoso", totalPoints = 1200, currentStreak = 5, weightKg = 70.0),
            UserEntity(username = "SitiRun", email = "siti@run.com", passwordHash = "siti123", fullName = "Siti Rahma", totalPoints = 850, currentStreak = 3, weightKg = 55.0),
            UserEntity(username = "KalcerRunner", email = "kalcer@run.com", passwordHash = "kalcer123", fullName = "Rian Kalcer", totalPoints = 1950, currentStreak = 12, weightKg = 68.0),
            UserEntity(username = "CoachAlex", email = "alex@run.com", passwordHash = "alex123", fullName = "Coach Alex", totalPoints = 2500, currentStreak = 20, weightKg = 75.0),
            UserEntity(username = "DianFit", email = "dian@run.com", passwordHash = "dian123", fullName = "Dian Lestari", totalPoints = 400, currentStreak = 1, weightKg = 60.0)
        )
        for (user in mockUsers) {
            val existing = userDao.getUserByUsername(user.username)
            if (existing == null) {
                userDao.insertUser(user)
            }
        }
    }
}
