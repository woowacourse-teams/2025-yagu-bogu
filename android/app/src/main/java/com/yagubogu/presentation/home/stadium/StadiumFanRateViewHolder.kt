package com.yagubogu.presentation.home.stadium

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemStadiumFanRateBinding

class StadiumFanRateViewHolder private constructor(
    private val binding: ItemStadiumFanRateBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: StadiumFanRateItem) {
        binding.stadiumFanRate = item
    }

    companion object {
        fun from(parent: ViewGroup): StadiumFanRateViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemStadiumFanRateBinding.inflate(inflater, parent, false)
            return StadiumFanRateViewHolder(binding)
        }
    }
}
