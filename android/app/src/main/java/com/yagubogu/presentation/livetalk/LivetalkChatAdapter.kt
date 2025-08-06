package com.yagubogu.presentation.livetalk

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class LivetalkChatAdapter : ListAdapter<LivetalkChatItem, LivetalkChatViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LivetalkChatViewHolder = LivetalkChatViewHolder.Companion.from(parent)

    override fun onBindViewHolder(
        holder: LivetalkChatViewHolder,
        position: Int,
    ) {
        val item: LivetalkChatItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<LivetalkChatItem>() {
                override fun areItemsTheSame(
                    oldItem: LivetalkChatItem,
                    newItem: LivetalkChatItem,
                ): Boolean = oldItem.chatId == newItem.chatId

                override fun areContentsTheSame(
                    oldItem: LivetalkChatItem,
                    newItem: LivetalkChatItem,
                ): Boolean = oldItem == newItem
            }
    }
}
