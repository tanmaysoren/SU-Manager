package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM location_ip_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<LocationIpHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: LocationIpHistory): Long

    @Delete
    suspend fun deleteHistory(item: LocationIpHistory)

    @Query("DELETE FROM location_ip_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM location_ip_history")
    suspend fun clearAll()

    @Query("UPDATE location_ip_history SET isActive = 0")
    suspend fun deactivateAll()
}
