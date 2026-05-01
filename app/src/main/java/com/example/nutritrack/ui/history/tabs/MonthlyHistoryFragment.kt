package com.example.nutritrack.ui.history.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nutritrack.databinding.FragmentMonthlyHistoryBinding
import com.example.nutritrack.ui.history.HistoryActivity
import com.example.nutritrack.ui.history.HistoryViewModel

class MonthlyHistoryFragment : Fragment() {
    private var _binding: FragmentMonthlyHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by lazy(LazyThreadSafetyMode.NONE) {
        (requireActivity() as HistoryActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.monthlySummary.observe(viewLifecycleOwner) { summary ->
            binding.tvMonthlyAverage.text = "Promedio diario: ${summary.promedioCalorias.toInt()} kcal"
            binding.tvOverGoal.text = "Dias sobre objetivo: ${summary.diasSobreObjetivo}"
            binding.tvUnderGoal.text = "Dias bajo objetivo: ${summary.diasBajoObjetivo}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
