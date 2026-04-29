package com.example.nutritrack.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutritrack.data.local.entities.DailyGoal

@Dao
interface DailyGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: DailyGoal)

    @Query("SELECT * FROM daily_goal WHERE id = 1")
    fun observeGoal(): LiveData<DailyGoal?>

    @Query("SELECT * FROM daily_goal WHERE id = 1")
    suspend fun getGoal(): DailyGoal?
}
