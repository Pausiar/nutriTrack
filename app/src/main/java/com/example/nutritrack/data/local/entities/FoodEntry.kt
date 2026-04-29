package com.example.nutritrack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val hora: String,
    val descripcion: String,
    val fotoPath: String?,
    val calorias: Double,
    val proteinas: Double,
    val carbos: Double,
    val grasas: Double,
    val fibra: Double,
    val azucares: Double,
    val sodio: Double
)
