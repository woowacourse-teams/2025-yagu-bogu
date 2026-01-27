package com.yagubogu.data.datasource.stream

import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import com.yagubogu.data.network.SseClient
import com.yagubogu.data.network.SseHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class StreamRemoteDataSource @Inject constructor(
    private val sseClient: SseClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
    kermitLogger: Logger,
) : StreamDataSource {
    private val logger = kermitLogger.withTag("StreamRemoteDataSource")
    private val eventFlow =
        MutableSharedFlow<SseCheckInResponse>(
            replay = 1,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    private val checkInSseHandler =
        object : SseHandler {
            override fun onConnectionOpened() {
                logger.d { "SSE connection opened" }
            }

            override fun onConnectionClosed() {
                logger.d { "SSE connection closed" }
            }

            override fun onEventReceived(
                event: String,
                data: String,
            ) {
                logger.d { "SSE event: $event, data: $data" }
                val response: SseCheckInResponse =
                    when (event) {
                        EVENT_TIMEOUT -> {
                            SseCheckInResponse.Timeout
                        }

                        EVENT_CONNECT -> {
                            SseCheckInResponse.Connect
                        }

                        EVENT_CHECK_IN_CREATED -> {
                            try {
                                val checkInItems: List<FanRateByGameDto> = json.decodeFromString(data)
                                SseCheckInResponse.CheckInCreated(checkInItems)
                            } catch (e: Exception) {
                                logger.e(e) { "SSE event 파싱 실패: $data" }
                                SseCheckInResponse.Unknown
                            }
                        }

                        else -> {
                            SseCheckInResponse.Unknown
                        }
                    }
                eventFlow.tryEmit(response)
            }

            override fun onComment(comment: String) {
                logger.d { "SSE comment: $comment" }
            }

            override fun onError(t: Throwable) {
                logger.e(t) { "SSE error: $t" }
            }
        }

    override fun connect(): Flow<SseCheckInResponse> {
        sseClient.connect(checkInSseHandler)
        return eventFlow
    }

    override fun disconnect() {
        sseClient.disconnect()
    }

    companion object {
        private const val EVENT_TIMEOUT = "timeout"
        private const val EVENT_CONNECT = "connect"
        private const val EVENT_CHECK_IN_CREATED = "check-in-created"
    }
}
