package com.yagubogu.presentation.favorite

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class FavoriteTeamListAdapter(
    private val favoriteTeamClickListener: OnItemClickListener,
) : ListAdapter<FavoriteTeamUiModel, FavoriteTeamViewHolder>(diffCallback) {
    interface OnItemClickListener {
        fun onItemClick(item: FavoriteTeamUiModel)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FavoriteTeamViewHolder = FavoriteTeamViewHolder.from(parent)

    override fun onBindViewHolder(
        holder: FavoriteTeamViewHolder,
        position: Int,
    ) {
        val item = currentList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            favoriteTeamClickListener.onItemClick(item)
        }
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
