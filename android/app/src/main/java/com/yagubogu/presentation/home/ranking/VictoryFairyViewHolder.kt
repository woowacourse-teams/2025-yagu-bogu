package com.yagubogu.presentation.home.ranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemVictoryFairyBinding

class VictoryFairyViewHolder private constructor(
    private val binding: ItemVictoryFairyBinding,
    handler: Handler,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.handler = handler
    }

    fun bind(item: VictoryFairyItem) {
        binding.victoryFairyItem = item
    }

    interface Handler {
        fun onProfileImageClick(memberId: Long)
    }

    companion object {
        fun from(
            parent: ViewGroup,
            handler: Handler,
        ): VictoryFairyViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemVictoryFairyBinding.inflate(inflater, parent, false)
            return VictoryFairyViewHolder(binding, handler)
        }
    }
}
