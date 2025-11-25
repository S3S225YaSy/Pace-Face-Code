package com.example.paceface

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_achievements",
    indices = [Index(value = ["userId"]), Index(value = ["achievementId"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Achievement::class,
            parentColumns = ["achievementId"],
            childColumns = ["achievementId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserAchievement(
    @PrimaryKey(autoGenerate = true)
    val userAchievementId: Int = 0,
    val userId: Int,
    val achievementId: Int,
    val achievedAt: Long
)
