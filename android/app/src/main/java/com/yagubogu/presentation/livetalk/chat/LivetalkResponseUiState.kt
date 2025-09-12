package com.yagubogu.presentation.livetalk.chat

sealed interface LivetalkResponseUiState {
    data object Success : LivetalkResponseUiState

    data object Loading : LivetalkResponseUiState

    data object Error : LivetalkResponseUiState
}
