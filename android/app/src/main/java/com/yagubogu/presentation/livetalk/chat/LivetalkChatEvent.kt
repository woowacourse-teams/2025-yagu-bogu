package com.yagubogu.presentation.livetalk.chat

sealed interface LivetalkChatEvent {
    data class Report(
        val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatEvent

    data class Delete(
        val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatEvent
}
