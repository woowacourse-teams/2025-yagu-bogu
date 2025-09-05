package com.yagubogu.presentation.favorite

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class FavoriteTeamAdapter(
    private val handler: FavoriteTeamViewHolder.Handler,
) : ListAdapter<FavoriteTeamItem, FavoriteTeamViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FavoriteTeamViewHolder = FavoriteTeamViewHolder.from(parent, handler)

    override fun onBindViewHolder(
        holder: FavoriteTeamViewHolder,
        position: Int,
    ) {
        val item: FavoriteTeamItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<FavoriteTeamItem>() {
                override fun areItemsTheSame(
                    oldItem: FavoriteTeamItem,
                    newItem: FavoriteTeamItem,
                ): Boolean = oldItem.team == newItem.team

                override fun areContentsTheSame(
                    oldItem: FavoriteTeamItem,
                    newItem: FavoriteTeamItem,
                ): Boolean = oldItem == newItem
            }
    }
}
