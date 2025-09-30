package com.yagubogu.data.datasource.stream

import com.launchdarkly.eventsource.MessageEvent
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
                messageEvent: MessageEvent,
            ) {
                Timber.d("SSE event: $event, data: ${messageEvent.data}")
                val response: SseResponse =
                    when (event) {
                        "timeout" -> SseResponse.Timeout
                        "connect" -> SseResponse.Connect
                        "check-in-created" -> {
                            val checkInItems: List<SseFanRateDto> =
                                json.decodeFromString(messageEvent.data)
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
                // 서버가 2XX 이외의 오류 응답시 com.launchdarkly.eventsource.StreamHttpErrorException: Server returned HTTP error 401 예외가 발생
                // 클라이언트에서 서버의 연결 유지 시간보다 짧게 설정시 error=com.launchdarkly.eventsource.StreamIOException: java.net.SocketTimeoutException: timeout 예외가 발생
                // 서버가 연결 유지 시간 초과로 종료시 error=com.launchdarkly.eventsource.StreamClosedByServerException: Stream closed by server 예외가 발생
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
}
