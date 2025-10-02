package com.yagubogu.data.datasource.stream

import com.yagubogu.data.dto.response.stream.SseFanRateDto
import com.yagubogu.data.dto.response.stream.SseResponse
import com.yagubogu.data.network.SseClient
import com.yagubogu.data.network.SseHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import timber.log.Timber

class StreamRemoteDataSource(
    private val sseClient: SseClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val eventFlow = MutableSharedFlow<SseResponse>(replay = 1)

    private val checkInSseHandler =
        object : SseHandler {
            override fun onConnectionOpened() {
                Timber.d("SSE connection opened")
            }

            override fun onConnectionClosed() {
                Timber.d("SSE connection closed")
            }

            override fun onEventReceived(
                event: String,
                data: String,
            ) {
                Timber.d("SSE event: $event, data: $data")
                val response: SseResponse =
                    when (event) {
                        EVENT_TIMEOUT -> SseResponse.Timeout
                        EVENT_CONNECT -> SseResponse.Connect
                        EVENT_CHECK_IN_CREATED -> {
                            val checkInItems: List<SseFanRateDto> =
                                json.decodeFromString(data)
                            SseResponse.CheckInCreated(checkInItems)
                        }

                        else -> SseResponse.Unknown
                    }
                eventFlow.tryEmit(response)
            }

            override fun onComment(comment: String) {
                Timber.d("SSE comment: $comment")
            }

            override fun onError(t: Throwable) {
                Timber.d("SSE error: $t")
            }
        }

    fun connect(): Flow<SseResponse> {
        sseClient.connect(checkInSseHandler)
        return eventFlow
    }

    fun disconnect() {
        sseClient.disconnect()
    }

    companion object {
        private const val EVENT_TIMEOUT = "timeout"
        private const val EVENT_CONNECT = "connect"
        private const val EVENT_CHECK_IN_CREATED = "check-in-created"
    }
}
