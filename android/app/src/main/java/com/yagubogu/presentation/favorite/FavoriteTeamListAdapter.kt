package com.yagubogu.presentation.favorite

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class FavoriteTeamListAdapter : ListAdapter<FavoriteTeamUiModel, FavoriteTeamViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FavoriteTeamViewHolder = FavoriteTeamViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: FavoriteTeamViewHolder,
        position: Int,
    ) {
        holder.bind(currentList[position])
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<FavoriteTeamUiModel>() {
                override fun areItemsTheSame(
                    oldItem: FavoriteTeamUiModel,
                    newItem: FavoriteTeamUiModel,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: FavoriteTeamUiModel,
                    newItem: FavoriteTeamUiModel,
                ): Boolean = oldItem == newItem
            }
    }
}
