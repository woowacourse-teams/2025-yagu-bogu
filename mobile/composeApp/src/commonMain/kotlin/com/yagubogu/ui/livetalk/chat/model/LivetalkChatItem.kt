package com.yagubogu.ui.livetalk.chat.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime

@Immutable
data class LivetalkChatItem(
    val chatId: Long,
    val memberId: Long,
    val isMine: Boolean,
    val message: String,
    val profileImageUrl: String?,
    val nickname: String?,
    val teamName: String?,
    val timestamp: LocalDateTime,
    val reported: Boolean,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
)
