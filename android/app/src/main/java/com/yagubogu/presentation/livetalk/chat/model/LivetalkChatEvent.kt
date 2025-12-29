package com.yagubogu.presentation.livetalk.chat.model

sealed interface LivetalkChatEvent {
    data class ViewProfile(
        val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatEvent

    data class Report(
        val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatEvent

    data class Delete(
        val livetalkChatItem: LivetalkChatItem,
    ) : LivetalkChatEvent
}
