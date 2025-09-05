package com.yagubogu.presentation.livetalk.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.databinding.ItemLivetalkOtherBubbleBinding

class LivetalkOtherBubbleViewHolder private constructor(
    private val binding: ItemLivetalkOtherBubbleBinding,
    private val livetalkChatEventHandler: LivetalkChatEventHandler,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LivetalkChatItem) {
        binding.livetalkChatItem = item

        binding.constraintReportContainer.setOnClickListener {
            livetalkChatEventHandler.onEvent(LivetalkChatEvent.Report(item))
        }
        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup,
            livetalkChatEventHandler: LivetalkChatEventHandler,
        ): LivetalkOtherBubbleViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLivetalkOtherBubbleBinding.inflate(inflater, parent, false)
            return LivetalkOtherBubbleViewHolder(binding, livetalkChatEventHandler)
        }
    }
}
