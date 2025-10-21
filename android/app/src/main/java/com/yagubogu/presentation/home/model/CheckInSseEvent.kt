package com.yagubogu.presentation.home.model

import com.yagubogu.presentation.home.stadium.StadiumFanRateItem

sealed interface CheckInSseEvent {
    data class CheckInCreated(
        val items: List<StadiumFanRateItem>,
    ) : CheckInSseEvent

    data object Timeout : CheckInSseEvent

    data object Connect : CheckInSseEvent

    data object Unknown : CheckInSseEvent
}
