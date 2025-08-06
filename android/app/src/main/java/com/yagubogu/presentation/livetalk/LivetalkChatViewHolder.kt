package com.yagubogu.presentation.livetalk

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemLivetalkChatBinding

class LivetalkChatViewHolder(
    private val binding: ItemLivetalkChatBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LivetalkChatItem) {
        binding.livetalkChatItem = item
    }

    companion object {
        fun from(parent: ViewGroup): LivetalkChatViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLivetalkChatBinding.inflate(inflater, parent, false)
            return LivetalkChatViewHolder(binding)
        }
    }
}
