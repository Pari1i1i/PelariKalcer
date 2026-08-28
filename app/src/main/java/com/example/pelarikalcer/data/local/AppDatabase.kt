package com.example.pelarikalcer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pelarikalcer.data.local.dao.*
import com.example.pelarikalcer.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        RunEntity::class,
        ChallengeEntity::class,
        UserChallengeEntity::class,
        PetEntity::class,
        AiCoachSessionEntity::class,
        AiCoachMessageEntity::class,
        FriendEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun runDao(): RunDao
    abstract fun aiCoachDao(): AiCoachDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun petDao(): PetDao

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
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
