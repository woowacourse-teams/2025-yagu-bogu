package com.yagubogu.data.repository.stream

import com.yagubogu.data.dto.response.stream.SseStreamResponse
import kotlinx.coroutines.flow.Flow

interface StreamRepository {
    fun connect(): Flow<SseStreamResponse>

    fun disconnect()
}
