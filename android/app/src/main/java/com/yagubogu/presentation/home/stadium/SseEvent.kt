package com.yagubogu.presentation.home.stadium

sealed interface SseEvent {
    data class CheckInCreated(
        val items: List<StadiumFanRateItem>,
    ) : SseEvent

    data object Timeout : SseEvent

    data object Connect : SseEvent

    data object Unknown : SseEvent
}
