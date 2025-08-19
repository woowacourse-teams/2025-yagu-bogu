package com.yagubogu.presentation.livetalk.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemLivetalkMyBubbleBinding

class LivetalkMyBubbleViewHolder(
    private val binding: ItemLivetalkMyBubbleBinding,
    private val viewModel: LivetalkChatViewModel,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LivetalkChatItem) {
        binding.livetalkChatItem = item
        binding.viewModel = viewModel
    }

    companion object {
        fun from(
            parent: ViewGroup,
            viewModel: LivetalkChatViewModel,
        ): LivetalkMyBubbleViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLivetalkMyBubbleBinding.inflate(inflater, parent, false)
            return LivetalkMyBubbleViewHolder(binding, viewModel)
        }
    }
}
