package com.yagubogu.ui.home.model

sealed interface CheckInSseEvent {
    data class CheckInCreated(
        val items: List<StadiumFanRateItem>,
    ) : CheckInSseEvent

    data class Connect(
        val data: String,
    ) : CheckInSseEvent

    data class Timeout(
        val data: String,
    ) : CheckInSseEvent

    data class Comment(
        val text: String,
    ) : CheckInSseEvent

    data object ConnectionOpened : CheckInSseEvent

    data object ConnectionClosed : CheckInSseEvent

    data class Error(
        val error: Exception,
    ) : CheckInSseEvent
}
