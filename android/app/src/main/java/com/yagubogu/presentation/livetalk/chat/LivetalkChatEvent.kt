package com.yagubogu.presentation.livetalk.chat

sealed interface LivetalkChatEvent {
    data class Report(
        val chatId: Long,
    ) : LivetalkChatEvent

    data class Delete(
        val chatId: Long,
    ) : LivetalkChatEvent
}
