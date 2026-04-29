package com.example.nutritrack.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.local.entities.FoodEntry
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.utils.DateUtils
import kotlinx.coroutines.launch

data class MonthlySummary(
    val promedioCalorias: Double,
    val diasSobreObjetivo: Int,
    val diasBajoObjetivo: Int
)

class HistoryViewModel(private val repository: FoodRepository) : ViewModel() {
    val dailyEntries: LiveData<List<FoodEntry>> = repository.observeEntriesByDate(DateUtils.todayDate())

    private val _weeklyEntries = MutableLiveData<List<FoodEntry>>(emptyList())
    val weeklyEntries: LiveData<List<FoodEntry>> = _weeklyEntries

    private val _monthlySummary = MutableLiveData<MonthlySummary>()
    val monthlySummary: LiveData<MonthlySummary> = _monthlySummary

    init {
        loadWeekly()
        loadMonthly()
    }

    fun loadWeekly() {
        viewModelScope.launch {
            val start = DateUtils.dateDaysAgo(6)
            val end = DateUtils.todayDate()
            _weeklyEntries.value = repository.getEntriesBetweenDates(start, end)
        }
    }

    fun loadMonthly() {
        viewModelScope.launch {
            val (start, end) = DateUtils.currentMonthRange()
            val entries = repository.getEntriesBetweenDates(start, end)
            val grouped = entries.groupBy { it.fecha }.mapValues { it.value.sumOf { e -> e.calorias } }
            val avg = if (grouped.isNotEmpty()) grouped.values.average() else 0.0
            val over = grouped.count { it.value > 2200.0 }
            val under = grouped.count { it.value <= 2200.0 }
            _monthlySummary.value = MonthlySummary(avg, over, under)
        }
    }
}

class HistoryViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
