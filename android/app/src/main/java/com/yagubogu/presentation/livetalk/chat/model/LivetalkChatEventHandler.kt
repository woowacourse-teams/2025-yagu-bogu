package com.yagubogu.presentation.livetalk.chat.model

fun interface LivetalkChatEventHandler {
    fun onEvent(event: LivetalkChatEvent)
}
