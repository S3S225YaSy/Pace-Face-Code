package com.example.paceface

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_status",
    indices = [Index(value = ["userId"]), Index(value = ["currentEmotionId"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Emotion::class,
            parentColumns = ["emotionId"],
            childColumns = ["currentEmotionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class DeviceStatus(
    @PrimaryKey(autoGenerate = true)
    val statusId: Int = 0,
    val userId: Int,
    val isConnected: Boolean,
    val batteryLevel: Int,
    val lastUpdatedAt: Long,
    val currentSpeed: Float,
    val currentEmotionId: Int?
)
