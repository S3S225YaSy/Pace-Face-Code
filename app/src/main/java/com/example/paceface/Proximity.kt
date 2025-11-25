package com.example.paceface

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "proximity",
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
data class Proximity(
    @PrimaryKey(autoGenerate = true)
    val proximityId: Int = 0,
    val userId: Int, // Self
    val passedUserId: Int, // The user who was passed
    val timestamp: Long,
    val isConfirmed: Boolean = false
)
