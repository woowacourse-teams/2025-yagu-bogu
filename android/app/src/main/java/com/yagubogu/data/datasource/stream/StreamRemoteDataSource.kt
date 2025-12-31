package com.yagubogu.data.datasource.stream

import com.yagubogu.data.dto.response.stream.SseStreamResponse
import com.yagubogu.data.network.SseClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StreamRemoteDataSource @Inject constructor(
    private val sseClient: SseClient,
) : StreamDataSource {
    override fun connect(): Flow<SseStreamResponse> = sseClient.connect()

    override fun disconnect() {
        sseClient.disconnect()
    }
}
