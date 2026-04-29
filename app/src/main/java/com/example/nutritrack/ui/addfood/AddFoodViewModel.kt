package com.example.nutritrack.ui.addfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.local.entities.FoodEntry
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.data.remote.models.NutritionAnalysis
import com.example.nutritrack.utils.DateUtils
import kotlinx.coroutines.launch

class AddFoodViewModel(private val repository: FoodRepository) : ViewModel() {
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun analyzeAndSaveText(description: String) {
        if (description.isBlank()) {
            _error.value = "Describe la comida"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val analysis = repository.analyzeTextMeal(description)
                saveEntry(description, null, analysis)
                _saved.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al analizar texto"
            } finally {
                _loading.value = false
            }
        }
    }

    fun analyzeAndSaveImage(description: String, imageBase64: String, imagePath: String?) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val analysis = repository.analyzeImageMeal(imageBase64, description)
                val resolvedDescription = description.ifBlank { analysis.alimento }
                saveEntry(resolvedDescription, imagePath, analysis)
                _saved.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al analizar imagen"
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun saveEntry(description: String, imagePath: String?, analysis: NutritionAnalysis) {
        repository.insertFoodEntry(
            FoodEntry(
                fecha = DateUtils.todayDate(),
                hora = DateUtils.nowTime(),
                descripcion = description,
                fotoPath = imagePath,
                calorias = analysis.calorias,
                proteinas = analysis.proteinasG,
                carbos = analysis.carbohidratosG,
                grasas = analysis.grasasG,
                fibra = analysis.fibraG,
                azucares = analysis.azucaresG,
                sodio = analysis.sodioMg
            )
        )
    }
}

class AddFoodViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddFoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
