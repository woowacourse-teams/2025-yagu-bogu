package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.stream.SseStreamResponse
import com.yagubogu.ui.home.model.CheckInSseEvent

fun SseStreamResponse.toUiModel(): CheckInSseEvent =
    when (this) {
        is SseStreamResponse.CheckInCreated -> CheckInSseEvent.CheckInCreated(items.map { it.toUiModel() })
        is SseStreamResponse.Connect -> CheckInSseEvent.Connect(data)
        is SseStreamResponse.Timeout -> CheckInSseEvent.Timeout(data)
        is SseStreamResponse.Comment -> CheckInSseEvent.Comment(data)
        SseStreamResponse.ConnectionOpened -> CheckInSseEvent.ConnectionOpened
        SseStreamResponse.ConnectionClosed -> CheckInSseEvent.ConnectionClosed
        is SseStreamResponse.Error -> CheckInSseEvent.Error(error)
    }
