package com.yagubogu.ui.home.model

sealed interface CheckInSseEvent {
    data class CheckInCreated(
        val items: List<StadiumFanRateItem>,
    ) : CheckInSseEvent

    data object Timeout : CheckInSseEvent

    data object Connect : CheckInSseEvent

    data object Unknown : CheckInSseEvent
}
