package com.yagubogu.presentation.livetalk.chat

sealed interface LivetalkUiState {
    data object Success : LivetalkUiState

    data object Loading : LivetalkUiState

    data object Error : LivetalkUiState
}
