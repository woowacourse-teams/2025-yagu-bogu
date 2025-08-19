package com.yagubogu.presentation.livetalk.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemLivetalkOtherBubbleBinding

class LivetalkOtherBubbleViewHolder(
    private val binding: ItemLivetalkOtherBubbleBinding,
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
        ): LivetalkOtherBubbleViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLivetalkOtherBubbleBinding.inflate(inflater, parent, false)
            return LivetalkOtherBubbleViewHolder(binding, viewModel)
        }
    }
}
