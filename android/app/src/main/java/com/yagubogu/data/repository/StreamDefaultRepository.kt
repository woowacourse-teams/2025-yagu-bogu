package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stream.StreamDataSource
import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import com.yagubogu.domain.repository.StreamRepository
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
