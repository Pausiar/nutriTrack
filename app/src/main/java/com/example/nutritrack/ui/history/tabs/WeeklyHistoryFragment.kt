package com.example.nutritrack.ui.history.tabs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nutritrack.databinding.FragmentWeeklyHistoryBinding
import com.example.nutritrack.ui.history.HistoryViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class WeeklyHistoryFragment : Fragment() {
    private var _binding: FragmentWeeklyHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<HistoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.weeklyEntries.observe(viewLifecycleOwner) { entries ->
            val grouped = entries.groupBy { it.fecha }.toSortedMap()
            val chartEntries = grouped.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.sumOf { it.calorias }.toFloat())
            }

            val dataSet = BarDataSet(chartEntries, "Calorias por dia").apply {
                color = Color.parseColor("#2E7D32")
                valueTextColor = Color.BLACK
            }

            binding.barChart.data = BarData(dataSet)
            binding.barChart.description.isEnabled = false
            binding.barChart.invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
