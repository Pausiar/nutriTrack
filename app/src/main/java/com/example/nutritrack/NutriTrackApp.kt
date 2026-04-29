package com.example.nutritrack

import android.app.Application
import com.example.nutritrack.data.local.AppDatabase
import com.example.nutritrack.data.repository.FoodRepository

class NutriTrackApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: FoodRepository by lazy {
        FoodRepository(database.foodEntryDao(), database.dailyGoalDao())
    }
}
