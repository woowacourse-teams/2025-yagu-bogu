package com.yagubogu.presentation.home.model

sealed class CheckInUiEvent {
    data class Success(
        val stadium: StadiumWithGame,
    ) : CheckInUiEvent()

    data object NoGame : CheckInUiEvent()

    data object OutOfRange : CheckInUiEvent()

    data object LocationFetchFailed : CheckInUiEvent()

    data object NetworkFailed : CheckInUiEvent()
}
