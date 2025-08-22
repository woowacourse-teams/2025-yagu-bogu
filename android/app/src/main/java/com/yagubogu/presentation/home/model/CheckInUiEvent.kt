package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Stadium

sealed class CheckInUiEvent {
    data class CheckInSuccess(
        val stadium: Stadium,
    ) : CheckInUiEvent()

    data object CheckInFailure : CheckInUiEvent()

    data object LocationFetchFailed : CheckInUiEvent()
}
