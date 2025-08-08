package com.yagubogu.presentation.livetalk.chat

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class LivetalkChatAdapter : ListAdapter<LivetalkChatBubbleItem, RecyclerView.ViewHolder>(diffCallback) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is LivetalkChatBubbleItem.MyBubbleItem -> VIEW_TYPE_MY
            is LivetalkChatBubbleItem.OtherBubbleItem -> VIEW_TYPE_OTHER
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_MY -> LivetalkMyBubbleViewHolder.from(parent)
            else -> LivetalkOtherBubbleViewHolder.from(parent)
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is LivetalkMyBubbleViewHolder ->
                holder.bind((getItem(position) as LivetalkChatBubbleItem.MyBubbleItem).livetalkChatItem)

            is LivetalkOtherBubbleViewHolder ->
                holder.bind((getItem(position) as LivetalkChatBubbleItem.OtherBubbleItem).livetalkChatItem)
        }
    }

    companion object {
        private const val VIEW_TYPE_MY = 0
        private const val VIEW_TYPE_OTHER = 1

        private val diffCallback =
            object : DiffUtil.ItemCallback<LivetalkChatBubbleItem>() {
                override fun areItemsTheSame(
                    oldItem: LivetalkChatBubbleItem,
                    newItem: LivetalkChatBubbleItem,
                ): Boolean {
                    return when {
                        oldItem is LivetalkChatBubbleItem.MyBubbleItem && newItem is LivetalkChatBubbleItem.MyBubbleItem ->
                            return oldItem.livetalkChatItem.chatId == newItem.livetalkChatItem.chatId

                        oldItem is LivetalkChatBubbleItem.OtherBubbleItem && newItem is LivetalkChatBubbleItem.OtherBubbleItem ->
                            return oldItem.livetalkChatItem.chatId == newItem.livetalkChatItem.chatId

                        else -> false
                    }
                }

                override fun areContentsTheSame(
                    oldItem: LivetalkChatBubbleItem,
                    newItem: LivetalkChatBubbleItem,
                ): Boolean = oldItem == newItem
            }
    }
}
