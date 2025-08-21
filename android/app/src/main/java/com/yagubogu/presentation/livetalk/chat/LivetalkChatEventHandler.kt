package com.yagubogu.presentation.livetalk.chat

fun interface LivetalkChatEventHandler {
    fun onEvent(event: LivetalkChatEvent)
}
