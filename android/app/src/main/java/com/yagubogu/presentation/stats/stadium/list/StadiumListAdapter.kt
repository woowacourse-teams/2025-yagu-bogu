package com.yagubogu.presentation.stats.stadium.list

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class StadiumListAdapter :
    ListAdapter<StadiumUiModel, StadiumListViewHolder>(
        object : DiffUtil.ItemCallback<StadiumUiModel>() {
            override fun areItemsTheSame(
                oldItem: StadiumUiModel,
                newItem: StadiumUiModel,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: StadiumUiModel,
                newItem: StadiumUiModel,
            ): Boolean = oldItem == newItem
        },
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): StadiumListViewHolder = StadiumListViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: StadiumListViewHolder,
        position: Int,
    ) {
        holder.bind(currentList[position])
    }
}
