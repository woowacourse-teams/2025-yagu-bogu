package com.yagubogu.presentation.home.ranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemVictoryFairyBinding

class VictoryFairyViewHolder private constructor(
    private val binding: ItemVictoryFairyBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: VictoryFairyItem) {
        binding.victoryFairyItem = item
    }

    companion object {
        fun from(parent: ViewGroup): VictoryFairyViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemVictoryFairyBinding.inflate(inflater, parent, false)
            return VictoryFairyViewHolder(binding)
        }
    }
}
