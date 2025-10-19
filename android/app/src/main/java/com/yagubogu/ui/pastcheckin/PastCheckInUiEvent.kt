package com.yagubogu.ui.pastcheckin

sealed class PastCheckInUiEvent {
    data class ShowToast(
        val message: String,
    ) : PastCheckInUiEvent()

    data class ShowSnackbar(
        val message: String,
    ) : PastCheckInUiEvent()
}
