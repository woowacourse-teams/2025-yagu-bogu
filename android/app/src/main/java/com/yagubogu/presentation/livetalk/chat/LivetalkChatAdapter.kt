package com.yagubogu.presentation.livetalk.chat

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatEventHandler

class LivetalkChatAdapter(
    private val livetalkChatEventHandler: LivetalkChatEventHandler,
) : ListAdapter<LivetalkChatBubbleItem, RecyclerView.ViewHolder>(diffCallback) {
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
            VIEW_TYPE_MY -> LivetalkMyBubbleViewHolder.from(parent, livetalkChatEventHandler)
            else -> LivetalkOtherBubbleViewHolder.from(parent, livetalkChatEventHandler)
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is LivetalkMyBubbleViewHolder -> {
                val item = getItem(position) as LivetalkChatBubbleItem.MyBubbleItem
                holder.bind(item.livetalkChatItem)
            }

            is LivetalkOtherBubbleViewHolder -> {
                val item = getItem(position) as LivetalkChatBubbleItem.OtherBubbleItem
                holder.bind(item.livetalkChatItem)
            }
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
                ): Boolean = oldItem.livetalkChatItem.chatId == newItem.livetalkChatItem.chatId

                override fun areContentsTheSame(
                    oldItem: LivetalkChatBubbleItem,
                    newItem: LivetalkChatBubbleItem,
                ): Boolean = oldItem == newItem
            }
    }
}
