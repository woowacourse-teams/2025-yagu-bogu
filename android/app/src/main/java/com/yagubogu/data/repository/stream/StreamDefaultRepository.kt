package com.yagubogu.data.repository.stream

import com.yagubogu.data.datasource.stream.StreamDataSource
import com.yagubogu.data.dto.response.stream.SseStreamResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StreamDefaultRepository @Inject constructor(
    private val streamDataSource: StreamDataSource,
) : StreamRepository {
    override fun connect(): Flow<SseStreamResponse> = streamDataSource.connect()

    override fun disconnect() {
        streamDataSource.disconnect()
    }
}
