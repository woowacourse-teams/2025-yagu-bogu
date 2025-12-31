package com.yagubogu.data.datasource.stream

import com.yagubogu.data.dto.response.stream.SseStreamResponse
import kotlinx.coroutines.flow.Flow

interface StreamDataSource {
    fun connect(): Flow<SseStreamResponse>

    fun disconnect()
}
