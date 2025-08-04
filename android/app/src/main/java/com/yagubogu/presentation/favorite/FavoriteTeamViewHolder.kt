package com.yagubogu.presentation.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemFavoriteTeamBinding

class FavoriteTeamViewHolder(
    private val binding: ItemFavoriteTeamBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(favoriteTeamUiModel: FavoriteTeamUiModel) {
        binding.favoriteTeamUiModel = favoriteTeamUiModel
    }

    interface Handler {
        fun onItemClick(item: FavoriteTeamUiModel)
    }

    companion object {
        fun from(
            parent: ViewGroup,
            handler: Handler,
        ): FavoriteTeamViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemFavoriteTeamBinding.inflate(inflater, parent, false)
            binding.handler = handler
            return FavoriteTeamViewHolder(binding)
        }
    }
}
