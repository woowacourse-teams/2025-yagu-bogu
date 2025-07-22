package com.yagubogu.presentation.stats.stadium.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemStadiumListBinding

class StadiumListViewHolder(
    private val binding: ItemStadiumListBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(stadiumUiModel: StadiumUiModel) {
        binding.stadium = stadiumUiModel
    }

    companion object {
        fun from(parent: ViewGroup): StadiumListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemStadiumListBinding.inflate(inflater, parent, false)
            return StadiumListViewHolder(binding)
        }
    }
}
