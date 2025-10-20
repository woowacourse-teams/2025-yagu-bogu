package com.yagubogu.presentation.home.model

sealed interface HomeDialogEvent {
    data class CheckInDialog(
        val stadium: Stadium,
    ) : HomeDialogEvent

    data object AdditionalCheckInDialog : HomeDialogEvent

    data class DoubleHeaderDialog(
        val stadium: Stadium,
    ) : HomeDialogEvent

    data object HideDialog : HomeDialogEvent
}
