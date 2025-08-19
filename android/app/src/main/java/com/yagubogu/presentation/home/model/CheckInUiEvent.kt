package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Stadium

sealed class CheckInUiEvent {
    data class Success(
        val stadium: Stadium,
    ) : CheckInUiEvent()

    data object OutOfRange : CheckInUiEvent()

    data object LocationFetchFailed : CheckInUiEvent()

    data object NetworkFailed : CheckInUiEvent()
}
