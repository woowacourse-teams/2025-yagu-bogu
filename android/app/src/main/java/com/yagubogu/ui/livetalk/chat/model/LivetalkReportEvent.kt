package com.yagubogu.ui.livetalk.chat.model

sealed class LivetalkReportEvent {
    data object Success : LivetalkReportEvent()

    data object DuplicatedReport : LivetalkReportEvent()
}
