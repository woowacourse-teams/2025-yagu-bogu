package com.yagubogu.presentation.home.model

sealed interface CheckInUiEvent {
    data class Success(
        val stadium: StadiumWithGame,
    ) : CheckInUiEvent

    data object NoGame : CheckInUiEvent

    data object OutOfRange : CheckInUiEvent

    data object AlreadyCheckedIn : CheckInUiEvent

    data object LocationFetchFailed : CheckInUiEvent

    data object NetworkFailed : CheckInUiEvent
}
