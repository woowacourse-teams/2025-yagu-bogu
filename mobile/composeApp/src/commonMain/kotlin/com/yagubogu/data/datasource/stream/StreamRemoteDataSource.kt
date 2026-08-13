package com.yagubogu.data.datasource.stream

import com.yagubogu.data.dto.response.stream.SseStreamResponse
import com.yagubogu.data.network.SseClient
import kotlinx.coroutines.flow.Flow

class StreamRemoteDataSource(
    private val sseClient: SseClient,
) : StreamDataSource {
    override fun connect(): Flow<SseStreamResponse> = sseClient.connect()
}
