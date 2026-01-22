package com.yagubogu.ui.attendance.model

sealed class PastGameUiState {
    data object Loading : PastGameUiState()

    data class Success(
        val pastGames: List<PastGameUiModel>,
    ) : PastGameUiState()
}
