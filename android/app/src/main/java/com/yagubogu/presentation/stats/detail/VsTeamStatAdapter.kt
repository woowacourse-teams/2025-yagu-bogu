package com.yagubogu.presentation.stats.detail

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class VsTeamStatAdapter : ListAdapter<VsTeamStatItem, VsTeamStatViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): VsTeamStatViewHolder = VsTeamStatViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: VsTeamStatViewHolder,
        position: Int,
    ) {
        val item: VsTeamStatItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<VsTeamStatItem>() {
                override fun areItemsTheSame(
                    oldItem: VsTeamStatItem,
                    newItem: VsTeamStatItem,
                ): Boolean = oldItem.team == newItem.team

                override fun areContentsTheSame(
                    oldItem: VsTeamStatItem,
                    newItem: VsTeamStatItem,
                ): Boolean = oldItem == newItem
            }
    }
}
