package com.example.paceface

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hourly_average_speed",
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
data class HourlyAverageSpeed(
    @PrimaryKey(autoGenerate = true)
    val hourlySpeedId: Int = 0,
    val userId: Int,
    val timestamp: Long, // Start hour timestamp
    val averageSpeed: Float
)
