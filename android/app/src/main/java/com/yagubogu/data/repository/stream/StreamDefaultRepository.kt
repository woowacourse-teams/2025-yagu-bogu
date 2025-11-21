package com.yagubogu.data.repository.stream

import com.yagubogu.data.datasource.stream.StreamDataSource
import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StreamDefaultRepository @Inject constructor(
    private val streamDataSource: StreamDataSource,
) : StreamRepository {
    override fun connect(): Flow<SseCheckInResponse> = streamDataSource.connect()

    override fun disconnect() {
        streamDataSource.disconnect()
    }
}
