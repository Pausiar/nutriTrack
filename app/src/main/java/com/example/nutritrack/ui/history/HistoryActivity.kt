package com.example.nutritrack.ui.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.nutritrack.NutriTrackApp
import com.example.nutritrack.databinding.ActivityHistoryBinding
import com.example.nutritrack.ui.history.tabs.DailyHistoryFragment
import com.example.nutritrack.ui.history.tabs.MonthlyHistoryFragment
import com.example.nutritrack.ui.history.tabs.WeeklyHistoryFragment
import com.google.android.material.tabs.TabLayoutMediator

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as NutriTrackApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int) = when (position) {
                0 -> DailyHistoryFragment()
                1 -> WeeklyHistoryFragment()
                else -> MonthlyHistoryFragment()
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Diaria"
                1 -> "Semanal"
                else -> "Mensual"
            }
        }.attach()
    }
}
