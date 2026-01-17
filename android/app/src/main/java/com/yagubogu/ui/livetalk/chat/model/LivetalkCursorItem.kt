package com.yagubogu.ui.livetalk.chat.model

data class LivetalkCursorItem(
    val chats: List<LivetalkChatItem>,
    val nextCursorId: Long?,
    val hasNext: Boolean,
)
