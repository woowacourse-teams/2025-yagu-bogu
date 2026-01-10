package com.yagubogu.ui.livetalk.chat.model

import com.yagubogu.domain.model.Team
import com.yagubogu.ui.common.model.MemberProfile

data class LivetalkChatScreenStates(
    val toolbar: Toolbar = Toolbar(),
    val chatList: ChatList = ChatList(),
    val inputBar: InputBar = InputBar(),
    val cheering: Cheering = Cheering(),
    val dialog: Dialog = Dialog(),
    val emojiLayer: EmojiLayer = EmojiLayer(),
    val isVerified: Boolean = false,
) {
    data class Toolbar(
        val teams: LivetalkTeams? = null,
    )

    data class ChatList(
        val uiState: LivetalkChatUiState = LivetalkChatUiState.Loading,
        val items: List<LivetalkChatBubbleItem> = emptyList(),
    )

    data class InputBar(
        val text: String = "",
        val stadiumName: String? = null,
    )

    data class Cheering(
        val myTeam: Team? = null,
        val showingCount: Long? = 0L,
    )

    data class Dialog(
        val clickedProfile: MemberProfile? = null,
        val pendingDeleteChat: LivetalkChatItem? = null,
        val pendingReportChat: LivetalkChatItem? = null,
    )

    data class EmojiLayer(
        val emojiQueue: List<EmojiAnimationItem> = emptyList(),
    )
}
