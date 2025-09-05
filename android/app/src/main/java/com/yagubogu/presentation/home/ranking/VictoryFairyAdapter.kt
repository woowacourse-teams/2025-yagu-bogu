package com.yagubogu.presentation.home.ranking

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class VictoryFairyAdapter : ListAdapter<VictoryFairyItem, VictoryFairyViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): VictoryFairyViewHolder = VictoryFairyViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: VictoryFairyViewHolder,
        position: Int,
    ) {
        val item: VictoryFairyItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<VictoryFairyItem>() {
                override fun areItemsTheSame(
                    oldItem: VictoryFairyItem,
                    newItem: VictoryFairyItem,
                ): Boolean = oldItem.rank == newItem.rank

                override fun areContentsTheSame(
                    oldItem: VictoryFairyItem,
                    newItem: VictoryFairyItem,
                ): Boolean = oldItem == newItem
            }
    }
}
