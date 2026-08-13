package com.yagubogu.ui.place.model

sealed interface PlaceListUiState {
    data object Loading : PlaceListUiState

    data class Success(
        val items: List<PlaceItem>,
    ) : PlaceListUiState

    data object Empty : PlaceListUiState

    data class Error(
        val message: String,
    ) : PlaceListUiState
}
