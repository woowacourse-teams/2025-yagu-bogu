package com.yagubogu.presentation.livetalk.chat

sealed interface LivetalkResponseUiState {
    data class LivetalkResponse(
        val livetalkResponseItem: LivetalkResponseItem,
    ) : LivetalkResponseUiState

    data object Loading : LivetalkResponseUiState

    data object Error : LivetalkResponseUiState
}
