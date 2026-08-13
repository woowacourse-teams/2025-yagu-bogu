package com.yagubogu.ui.attendance.detail.model

import org.jetbrains.compose.resources.StringResource

sealed interface AttendanceDetailUiEvent {
    data class ShowSnackbar(
        val message: StringResource,
    ) : AttendanceDetailUiEvent

    data object DeleteSuccess : AttendanceDetailUiEvent
}
