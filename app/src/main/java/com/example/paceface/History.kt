package com.example.paceface

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [Index(value = ["userId"]), Index(value = ["emotionId"])],
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
            childColumns = ["emotionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class History(
    @PrimaryKey(autoGenerate = true)
    val historyId: Int = 0,
    val userId: Int,
    val timestamp: Long,
    val walkingSpeed: Float,
    val acceleration: String, // e.g., "x,y,z"
    val emotionId: Int?
)
