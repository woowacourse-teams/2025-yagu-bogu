package com.yagubogu.data.dto.response.stream

import com.yagubogu.data.dto.response.checkin.FanRateByGameDto

sealed interface SseCheckInResponse {
    data object Timeout : SseCheckInResponse

    data object Connect : SseCheckInResponse

    data class CheckInCreated(
        val items: List<FanRateByGameDto>,
    ) : SseCheckInResponse

    data object Unknown : SseCheckInResponse
}
