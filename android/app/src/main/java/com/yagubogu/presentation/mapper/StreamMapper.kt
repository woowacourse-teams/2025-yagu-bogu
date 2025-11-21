package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import com.yagubogu.presentation.home.model.CheckInSseEvent
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem

fun SseCheckInResponse.toUiModel(): CheckInSseEvent =
    when (this) {
        is SseCheckInResponse.CheckInCreated -> {
            val items: List<StadiumFanRateItem> = items.map { it.toUiModel() }
            CheckInSseEvent.CheckInCreated(items)
        }

        SseCheckInResponse.Connect -> CheckInSseEvent.Connect
        SseCheckInResponse.Timeout -> CheckInSseEvent.Timeout
        SseCheckInResponse.Unknown -> CheckInSseEvent.Unknown
    }
