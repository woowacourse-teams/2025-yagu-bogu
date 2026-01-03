package com.yagubogu.ui.livetalk.chat.model

import androidx.compose.ui.geometry.Offset

data class LivetalkChatScreenActions(
    val onMessageTextChange: (String) -> Unit = {},
    val onRequestDelete: (LivetalkChatItem) -> Unit = {},
    val onRequestReport: (LivetalkChatItem) -> Unit = {},
    val onDismissDeleteDialog: () -> Unit = {},
    val onDismissReportDialog: () -> Unit = {},
    val onBackClick: () -> Unit = {},
    val onEmojiButtonPositioned: (Offset) -> Unit = {},
    val onCheeringClick: (String) -> Unit = {},
    val onAnimationFinished: (EmojiAnimationItem) -> Unit = {},
    val onSendMessage: () -> Unit = {},
    val onFetchMemberProfile: (Long) -> Unit = {},
    val onFetchBeforeTalks: () -> Unit = {},
    val onDeleteMessage: (Long) -> Unit = {},
    val onReportMessage: (Long) -> Unit = {},
    val onDismissProfile: () -> Unit = {},
)
