package com.yagubogu.data.repository

import com.yagubogu.data.datasource.stream.StreamDataSource
import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import com.yagubogu.domain.repository.StreamRepository
import com.yagubogu.presentation.home.model.CheckInSseEvent
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StreamDefaultRepository
    @Inject
    constructor(
        private val streamDataSource: StreamDataSource,
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
