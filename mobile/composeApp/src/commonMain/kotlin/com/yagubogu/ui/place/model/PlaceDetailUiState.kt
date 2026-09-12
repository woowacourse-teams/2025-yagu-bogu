package com.yagubogu.ui.place.model

sealed interface PlaceDetailUiState {
    data object Loading : PlaceDetailUiState

    data class Success(
        val detail: PlaceDetailUiModel,
    ) : PlaceDetailUiState

    data class Error(
        val message: String,
    ) : PlaceDetailUiState
}
