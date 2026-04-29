package com.example.nutritrack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goal")
data class DailyGoal(
    @PrimaryKey val id: Int = 1,
    val caloriasObjetivo: Double,
    val proteinasObjetivo: Double,
    val carbosObjetivo: Double,
    val grasasObjetivo: Double
)
