package com.yagubogu.ui.livetalk.chat.model

interface LivetalkChatUiState {
    data object Loading : LivetalkChatUiState

    data class Success(
        val chatItems: List<LivetalkChatBubbleItem>,
    ) : LivetalkChatUiState

    data object Empty : LivetalkChatUiState
}
