package com.yagubogu.presentation.favorite

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class FavoriteTeamListAdapter(
    private val handler: Handler,
) : ListAdapter<FavoriteTeamUiModel, FavoriteTeamViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FavoriteTeamViewHolder = FavoriteTeamViewHolder.from(parent, handler)

    override fun onBindViewHolder(
        holder: FavoriteTeamViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            handler.onItemClick(item)
        }
    }

    interface Handler : FavoriteTeamViewHolder.Handler

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
