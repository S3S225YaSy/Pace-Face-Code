package com.example.paceface

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emotions")
data class Emotion(
    @PrimaryKey(autoGenerate = true)
    val emotionId: Int = 0,
    val name: String,
    val imageUrl: String,
    val description: String
)
