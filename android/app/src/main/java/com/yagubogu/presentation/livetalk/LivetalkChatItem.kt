package com.yagubogu.presentation.livetalk

import java.time.LocalDateTime

data class LivetalkChatItem(
    val chatId: Long,
    val isMine: Boolean,
    val message: String,
    val profileImageUrl: String,
    val nickname: String,
    val teamName: String,
    val timestamp: LocalDateTime,
)
