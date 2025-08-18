package com.yagubogu.presentation.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemFavoriteTeamBinding

class FavoriteTeamViewHolder(
    private val binding: ItemFavoriteTeamBinding,
    handler: Handler,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.handler = handler
    }

    fun bind(favoriteTeamItem: FavoriteTeamItem) {
        binding.favoriteTeamItem = favoriteTeamItem
    }

    interface Handler {
        fun onItemClick(item: FavoriteTeamItem)
    }

    companion object {
        fun from(
            parent: ViewGroup,
            handler: Handler,
        ): FavoriteTeamViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemFavoriteTeamBinding.inflate(inflater, parent, false)
            return FavoriteTeamViewHolder(binding, handler)
        }
    }
}
