package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import com.yagubogu.ui.home.model.CheckInSseEvent

fun SseCheckInResponse.toUiModel(): CheckInSseEvent =
    when (this) {
        is SseCheckInResponse.CheckInCreated -> CheckInSseEvent.CheckInCreated(items.map { it.toUiModel() })
        SseCheckInResponse.Connect -> CheckInSseEvent.Connect
        SseCheckInResponse.Timeout -> CheckInSseEvent.Timeout
        SseCheckInResponse.Unknown -> CheckInSseEvent.Unknown
    }
