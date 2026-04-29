package com.example.nutritrack.ui.history.tabs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritrack.data.local.entities.FoodEntry
import com.example.nutritrack.databinding.ItemFoodEntryBinding

class FoodEntryAdapter : RecyclerView.Adapter<FoodEntryAdapter.FoodEntryViewHolder>() {
    private val items = mutableListOf<FoodEntry>()

    fun submitList(list: List<FoodEntry>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodEntryViewHolder {
        val binding = ItemFoodEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodEntryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class FoodEntryViewHolder(private val binding: ItemFoodEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FoodEntry) {
            binding.tvDescription.text = item.descripcion
            binding.tvDetail.text = "${item.fecha} ${item.hora}"
            binding.tvCalories.text = "${item.calorias.toInt()} kcal"
        }
    }
}
