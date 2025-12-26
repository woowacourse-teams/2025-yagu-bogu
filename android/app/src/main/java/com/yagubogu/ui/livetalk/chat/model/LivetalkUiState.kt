package com.yagubogu.ui.livetalk.chat.model

sealed interface LivetalkUiState {
    data object Success : LivetalkUiState

    data object Loading : LivetalkUiState

    data object Error : LivetalkUiState
}
