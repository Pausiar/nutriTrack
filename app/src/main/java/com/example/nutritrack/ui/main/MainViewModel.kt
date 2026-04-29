package com.example.nutritrack.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.local.entities.DailyGoal
import com.example.nutritrack.data.local.entities.FoodEntry
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.utils.DateUtils
import kotlinx.coroutines.launch

data class MainSummaryUi(
    val calorias: Double,
    val proteinas: Double,
    val carbos: Double,
    val grasas: Double,
    val objetivoCalorias: Double,
    val objetivoProteinas: Double,
    val objetivoCarbos: Double,
    val objetivoGrasas: Double
)

class MainViewModel(private val repository: FoodRepository) : ViewModel() {
    val todayEntries: LiveData<List<FoodEntry>> = repository.observeEntriesByDate(DateUtils.todayDate())
    val dailyGoal: LiveData<DailyGoal?> = repository.observeDailyGoal()

    val summary = MediatorLiveData<MainSummaryUi>()

    init {
        summary.addSource(todayEntries) { updateSummary() }
        summary.addSource(dailyGoal) { updateSummary() }

        viewModelScope.launch {
            repository.ensureDefaultGoal()
        }
    }

    private fun updateSummary() {
        val entries = todayEntries.value.orEmpty()
        val goal = dailyGoal.value ?: DailyGoal(
            caloriasObjetivo = 2200.0,
            proteinasObjetivo = 120.0,
            carbosObjetivo = 250.0,
            grasasObjetivo = 70.0
        )

        summary.value = MainSummaryUi(
            calorias = entries.sumOf { it.calorias },
            proteinas = entries.sumOf { it.proteinas },
            carbos = entries.sumOf { it.carbos },
            grasas = entries.sumOf { it.grasas },
            objetivoCalorias = goal.caloriasObjetivo,
            objetivoProteinas = goal.proteinasObjetivo,
            objetivoCarbos = goal.carbosObjetivo,
            objetivoGrasas = goal.grasasObjetivo
        )
    }
}

class MainViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
