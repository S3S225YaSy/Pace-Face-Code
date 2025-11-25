package com.example.paceface

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GraphDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyAverageSpeed(speedData: HourlyAverageSpeed)

    @Query("""
        SELECT * FROM hourly_average_speed
        WHERE userId = :userId AND timestamp >= :startOfDay AND timestamp < :endOfDay
        ORDER BY timestamp ASC
        """)
    suspend fun getHourlyAverageSpeedForDate(userId: Int, startOfDay: Long, endOfDay: Long): List<HourlyAverageSpeed>

    @Query("""
        SELECT * FROM hourly_emotion_percentage
        WHERE userId = :userId AND timestamp >= :startOfDay AND timestamp < :endOfDay
        ORDER BY timestamp ASC, emotionId ASC
        """)
    suspend fun getHourlyEmotionPercentageForDate(userId: Int, startOfDay: Long, endOfDay: Long): List<HourlyEmotionPercentage>

}
