package com.example.pelarikalcer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pelarikalcer.data.local.dao.AiCoachDao
import com.example.pelarikalcer.data.local.dao.RunDao
import com.example.pelarikalcer.data.local.dao.UserDao
import com.example.pelarikalcer.data.local.entity.*
import com.example.pelarikalcer.data.local.dao.ChallengeDao

@Database(
    entities = [
        UserEntity::class,
        RunEntity::class,
        ChallengeEntity::class,
        UserChallengeEntity::class,
        PetEntity::class,
        UserPetEntity::class,
        AiCoachSessionEntity::class,
        AiCoachMessageEntity::class,
        FriendEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun runDao(): RunDao
    abstract fun aiCoachDao(): AiCoachDao
    abstract fun challengeDao(): ChallengeDao   // baris baru

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pelarikalcer_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
