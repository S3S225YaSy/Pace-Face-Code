package com.example.paceface

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        History::class,
        Emotion::class,
        SpeedRule::class,
        DeviceStatus::class,
        Proximity::class,
        Achievement::class, // Renamed from Badge
        UserAchievement::class, // Renamed from UserBadge
        DailyAverageSpeed::class,
        HourlyAverageSpeed::class,
        HourlyEmotionPercentage::class
    ],
    version = 3, // Incremented version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun emotionDao(): EmotionDao
    abstract fun graphDataDao(): GraphDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paceface_database"
                )
                .fallbackToDestructiveMigration() // On version change, recreate the DB
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
