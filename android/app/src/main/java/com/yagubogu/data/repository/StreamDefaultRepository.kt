package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stream.StreamRemoteDataSource
import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import com.yagubogu.domain.repository.StreamRepository
import com.yagubogu.presentation.home.model.CheckInSseEvent
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StreamDefaultRepository(
    private val streamDataSource: StreamRemoteDataSource,
) : StreamRepository {
    override fun connect(): Flow<CheckInSseEvent> =
        streamDataSource.connect().map { sseResponse: SseCheckInResponse ->
            when (sseResponse) {
                is SseCheckInResponse.CheckInCreated -> {
                    val items: List<StadiumFanRateItem> =
                        sseResponse.items.map { it.toPresentation() }
                    CheckInSseEvent.CheckInCreated(items)
                }

                SseCheckInResponse.Connect -> CheckInSseEvent.Connect
                SseCheckInResponse.Timeout -> CheckInSseEvent.Timeout
                SseCheckInResponse.Unknown -> CheckInSseEvent.Unknown
            }
        }

    override fun disconnect() {
        streamDataSource.disconnect()
    }
}
