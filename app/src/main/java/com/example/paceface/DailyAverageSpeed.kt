package com.example.paceface

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_average_speed",
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyAverageSpeed(
    @PrimaryKey(autoGenerate = true)
    val dailySpeedId: Int = 0,
    val userId: Int,
    val date: Long, // Midnight timestamp of the date
    val averageSpeed: Float
)
