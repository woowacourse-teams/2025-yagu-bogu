package com.yagubogu.presentation.stats.detail

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class VsTeamStatAdapter : ListAdapter<VsTeamStat, VsTeamStatViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): VsTeamStatViewHolder = VsTeamStatViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: VsTeamStatViewHolder,
        position: Int,
    ) {
        val item: VsTeamStat = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<VsTeamStat>() {
                override fun areItemsTheSame(
                    oldItem: VsTeamStat,
                    newItem: VsTeamStat,
                ): Boolean = oldItem.rank == newItem.rank

                override fun areContentsTheSame(
                    oldItem: VsTeamStat,
                    newItem: VsTeamStat,
                ): Boolean = oldItem == newItem
            }
    }
}
