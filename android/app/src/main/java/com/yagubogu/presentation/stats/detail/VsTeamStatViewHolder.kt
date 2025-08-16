package com.yagubogu.presentation.stats.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemVsTeamStatBinding

class VsTeamStatViewHolder(
    private val binding: ItemVsTeamStatBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: VsTeamStat) {
        binding.vsTeamStat = item
    }

    companion object {
        fun from(parent: ViewGroup): VsTeamStatViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemVsTeamStatBinding.inflate(inflater, parent, false)
            return VsTeamStatViewHolder(binding)
        }
    }
}
