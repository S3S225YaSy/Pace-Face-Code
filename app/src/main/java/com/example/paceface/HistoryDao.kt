package com.example.paceface

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: History)

    @Query("SELECT * FROM History WHERE userId = :userId AND timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp ASC")
    suspend fun getHistoryForUserOnDate(userId: Int, startOfDay: Long, endOfDay: Long): List<History>

    @Query("DELETE FROM History WHERE userId = :userId AND timestamp BETWEEN :startOfDay AND :endOfDay")
    suspend fun deleteHistoryForUserOnDate(userId: Int, startOfDay: Long, endOfDay: Long)
}
