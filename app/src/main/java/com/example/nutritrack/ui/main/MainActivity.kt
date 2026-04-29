package com.example.nutritrack.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.nutritrack.NutriTrackApp
import com.example.nutritrack.databinding.ActivityMainBinding
import com.example.nutritrack.ui.addfood.AddFoodActivity
import com.example.nutritrack.ui.history.HistoryActivity
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as NutriTrackApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddFood.setOnClickListener {
            startActivity(Intent(this, AddFoodActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        viewModel.summary.observe(this) { summary ->
            binding.tvCaloriesSummary.text = "${summary.calorias.roundToInt()} / ${summary.objetivoCalorias.roundToInt()} kcal"
            binding.pbCalories.max = summary.objetivoCalorias.roundToInt().coerceAtLeast(1)
            binding.pbCalories.progress = summary.calorias.roundToInt().coerceAtLeast(0)

            updateMacroProgress(
                summary.proteinas,
                summary.objetivoProteinas,
                binding.pbProtein,
                binding.tvProtein
            )
            updateMacroProgress(
                summary.carbos,
                summary.objetivoCarbos,
                binding.pbCarbs,
                binding.tvCarbs
            )
            updateMacroProgress(
                summary.grasas,
                summary.objetivoGrasas,
                binding.pbFats,
                binding.tvFats
            )
        }
    }

    private fun updateMacroProgress(value: Double, goal: Double, progressBar: android.widget.ProgressBar, label: android.widget.TextView) {
        progressBar.max = goal.roundToInt().coerceAtLeast(1)
        progressBar.progress = value.roundToInt().coerceAtLeast(0)
        label.text = "${value.roundToInt()} / ${goal.roundToInt()} g"
    }
}
