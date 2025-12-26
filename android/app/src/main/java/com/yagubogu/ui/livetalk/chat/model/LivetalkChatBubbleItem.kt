package com.yagubogu.ui.livetalk.chat.model

sealed class LivetalkChatBubbleItem {
    abstract val livetalkChatItem: LivetalkChatItem

    data class MyPendingBubbleItem(
        override val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatBubbleItem()

    data class MyBubbleItem(
        override val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatBubbleItem()

    data class OtherBubbleItem(
        override val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatBubbleItem()

    companion object {
        fun of(livetalkChatItem: LivetalkChatItem): LivetalkChatBubbleItem =
            when (livetalkChatItem.isMine) {
                true -> MyBubbleItem(livetalkChatItem)
                false -> OtherBubbleItem(livetalkChatItem)
            }
    }
}
