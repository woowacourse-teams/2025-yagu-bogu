package com.yagubogu.presentation.livetalk.stadium

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemLivetalkStadiumBinding

class LivetalkStadiumViewHolder(
    private val binding: ItemLivetalkStadiumBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LivetalkStadiumItem) {
        binding.livetalkStadiumItem = item
    }

    companion object {
        fun from(parent: ViewGroup): LivetalkStadiumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLivetalkStadiumBinding.inflate(inflater, parent, false)
            return LivetalkStadiumViewHolder(binding)
        }
    }
}
