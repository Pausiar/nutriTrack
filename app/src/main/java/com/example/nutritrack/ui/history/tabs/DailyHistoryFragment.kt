package com.example.nutritrack.ui.history.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritrack.databinding.FragmentDailyHistoryBinding
import com.example.nutritrack.ui.history.HistoryActivity

class DailyHistoryFragment : Fragment() {
    private var _binding: FragmentDailyHistoryBinding? = null
    private val binding get() = _binding!!
    private val adapter = FoodEntryAdapter()

    private val viewModel by activityViewModels<com.example.nutritrack.ui.history.HistoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerDaily.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDaily.adapter = adapter

        viewModel.dailyEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            val total = entries.sumOf { it.calorias }
            binding.tvDailyTotal.text = "Total del dia: ${total.toInt()} kcal"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
