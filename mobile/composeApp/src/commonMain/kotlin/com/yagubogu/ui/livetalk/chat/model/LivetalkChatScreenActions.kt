package com.yagubogu.ui.livetalk.chat.model

import androidx.compose.ui.geometry.Offset
import org.jetbrains.compose.resources.DrawableResource

data class LivetalkChatScreenActions(
    val chatToolbar: ChatToolbar = ChatToolbar(),
    val chatInputBar: ChatInputBar = ChatInputBar(),
    val chatBubbleItems: ChatBubbleItems = ChatBubbleItems(),
    val chatCheering: ChatCheering = ChatCheering(),
    val floatingMascotItem: FloatingMascotItem = FloatingMascotItem(),
    val dialog: Dialog = Dialog(),
) {
    data class ChatToolbar(
        val onBackClick: () -> Unit = {},
    )

    data class ChatInputBar(
        val onMessageTextChange: (String) -> Unit = {},
        val onSendMessage: () -> Unit = {},
    )

    data class ChatBubbleItems(
        val onRequestDelete: (LivetalkChatItem) -> Unit = {},
        val onRequestReport: (LivetalkChatItem) -> Unit = {},
        val onFetchMemberProfile: (Long) -> Unit = {},
        val onFetchBeforeTalks: () -> Unit = {},
    )

    data class ChatCheering(
        val onCheeringClick: (DrawableResource) -> Unit = {},
        val onMascotButtonPositioned: (Offset) -> Unit = {},
    )

    data class FloatingMascotItem(
        val onAnimationFinished: (MascotAnimationItem) -> Unit = {},
    )

    data class Dialog(
        val onDeleteMessage: (Long) -> Unit = {},
        val onReportMessage: (Long) -> Unit = {},
        val onDismissProfile: () -> Unit = {},
        val onDismissDeleteDialog: () -> Unit = {},
        val onDismissReportDialog: () -> Unit = {},
    )
}
