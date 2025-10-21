package com.yagubogu.ui.pastcheckin

import androidx.annotation.StringRes

sealed class PastCheckInUiEvent {
    data class ShowToast(
        @StringRes
        val messageRes: Int,
    ) : PastCheckInUiEvent()

    data class ShowSnackbar(
        @StringRes
        val messageRes: Int,
    ) : PastCheckInUiEvent()

    data class ShowToastWithArgs(
        @StringRes val messageRes: Int,
        val args: List<Any>,
    ) : PastCheckInUiEvent()
}
