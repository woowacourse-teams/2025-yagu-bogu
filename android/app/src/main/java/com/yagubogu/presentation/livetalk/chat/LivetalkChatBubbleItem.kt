package com.yagubogu.presentation.livetalk.chat

sealed class LivetalkChatBubbleItem {
    data class MyBubbleItem(
        val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatBubbleItem()

    data class OtherBubbleItem(
        val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatBubbleItem()
}
