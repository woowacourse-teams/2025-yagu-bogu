package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Stadium

sealed class HomeUiEvent {
    data class CheckInSuccess(
        val stadium: Stadium,
    ) : HomeUiEvent()

    data object CheckInFailure : HomeUiEvent()

    data object LocationFetchFailed : HomeUiEvent()
}
