package com.yagubogu.ui.livetalk.chat.model

import com.yagubogu.ui.common.model.MemberProfile

data class LivetalkChatScreenStates(
    val messageText: String = "",
    val showingLikeCount: Long? = 0L,
    val livetalkChatBubbleItems: List<LivetalkChatBubbleItem> = emptyList(),
    val pendingDeleteChat: LivetalkChatItem? = null,
    val pendingReportChat: LivetalkChatItem? = null,
    val emojiQueue: List<EmojiAnimationItem> = emptyList(),
    val teams: LivetalkTeams? = null,
    val clickedProfile: MemberProfile? = null,
    val chatUiState: LivetalkChatUiState = LivetalkChatUiState.Loading,
    val isVerified: Boolean = false,
)
