package com.yagubogu.presentation.livetalk.chat.model

sealed class LivetalkReportEvent {
    data object Success : LivetalkReportEvent()

    data object DuplicatedReport : LivetalkReportEvent()
}
