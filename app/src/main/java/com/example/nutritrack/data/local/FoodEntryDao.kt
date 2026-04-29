package com.example.nutritrack.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutritrack.data.local.entities.FoodEntry

@Dao
interface FoodEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(entry: FoodEntry): Long

    @Query("SELECT * FROM food_entries WHERE fecha = :date ORDER BY hora DESC")
    fun observeEntriesByDate(date: String): LiveData<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE fecha = :date ORDER BY hora DESC")
    suspend fun getEntriesByDate(date: String): List<FoodEntry>

    @Query("SELECT * FROM food_entries ORDER BY fecha DESC, hora DESC")
    fun observeAllEntries(): LiveData<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE fecha BETWEEN :startDate AND :endDate ORDER BY fecha ASC, hora ASC")
    suspend fun getEntriesBetweenDates(startDate: String, endDate: String): List<FoodEntry>
}
