package com.yagubogu.ui.attendance.detail.model

import androidx.compose.runtime.Immutable
import com.yagubogu.ui.share.AttendanceTicketShareData

@Immutable
data class AttendanceDetailShareUiState(
    val shareData: AttendanceTicketShareData? = null,
    val isLoaded: Boolean = false,
)
