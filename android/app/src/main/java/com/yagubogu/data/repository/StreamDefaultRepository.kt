package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stream.StreamRemoteDataSource
import com.yagubogu.data.dto.response.stream.SseResponse
import com.yagubogu.domain.repository.StreamRepository
import com.yagubogu.presentation.home.model.CheckInSseEvent
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StreamDefaultRepository(
    private val streamDataSource: StreamRemoteDataSource,
) : StreamRepository {
    override suspend fun connect(): Flow<CheckInSseEvent> =
        streamDataSource.connect().map { sseResponse: SseResponse ->
            when (sseResponse) {
                is SseResponse.CheckInCreated -> {
                    val items: List<StadiumFanRateItem> =
                        sseResponse.items.map { it.toPresentation() }
                    CheckInSseEvent.CheckInCreated(items)
                }

                SseResponse.Connect -> CheckInSseEvent.Connect
                SseResponse.Timeout -> CheckInSseEvent.Timeout
                SseResponse.Unknown -> CheckInSseEvent.Unknown
            }
        }

    override fun disconnect() {
        streamDataSource.disconnect()
    }
}
