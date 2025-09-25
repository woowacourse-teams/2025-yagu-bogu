package com.yagubogu.data.dto.response.stream

sealed interface SseResponse {
    data object Timeout : SseResponse

    data object Connect : SseResponse

    data class CheckInCreated(
        val items: List<SseFanRateDto>,
    ) : SseResponse

    data object Unknown : SseResponse
}
