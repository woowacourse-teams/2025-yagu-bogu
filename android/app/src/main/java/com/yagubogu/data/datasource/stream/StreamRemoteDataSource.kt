package com.yagubogu.data.datasource.stream

import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.yagubogu.data.dto.response.stream.SseFanRateDto
import com.yagubogu.data.dto.response.stream.SseResponse
import com.yagubogu.data.network.SseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import timber.log.Timber

class StreamRemoteDataSource(
    private val sseClient: SseClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val eventFlow = MutableSharedFlow<SseResponse>(replay = 1)

    private val eventHandler =
        object : BackgroundEventHandler {
            // SSE 연결 성공
            override fun onOpen() {
                Timber.d("SSE onOpen")
            }

            // SSE 연결 종료
            override fun onClosed() {
                Timber.d("SSE onClosed")
                sseClient.disconnect()
            }

            // SSE 이벤트 도착
            override fun onMessage(
                event: String,
                messageEvent: MessageEvent,
            ) {
                // event: String = 이벤트가 속한 채널 또는 토픽 이름
                // messageEvent.data: String = 도착한 이벤트 데이터
                Timber.d("SSE onMessage: $messageEvent")
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
//                val response: SseResponse = SseResponse.CheckInCreated(json.decodeFromString("[{\"gameId\":101,\"homeTeam\":{\"name\":\"LG 트윈스\",\"code\":\"LG\",\"fanRate\":0.64},\"awayTeam\":{\"name\":\"두산 베어스\",\"code\":\"OB\",\"fanRate\":0.36}},{\"gameId\":102,\"homeTeam\":{\"name\":\"SSG 렌더스\",\"code\":\"SK\",\"fanRate\":0.55},\"awayTeam\":{\"name\":\"KIA 타이거즈\",\"code\":\"HT\",\"fanRate\":0.45}}]"))
                eventFlow.tryEmit(response)
            }

            override fun onComment(comment: String) {
                Timber.d("SSE onComment: $comment")
            }

            // SSE 연결 오류 발생
            override fun onError(t: Throwable) {
                // 서버가 2XX 이외의 오류 응답시 com.launchdarkly.eventsource.StreamHttpErrorException: Server returned HTTP error 401 예외가 발생
                // 클라이언트에서 서버의 연결 유지 시간보다 짧게 설정시 error=com.launchdarkly.eventsource.StreamIOException: java.net.SocketTimeoutException: timeout 예외가 발생
                // 서버가 연결 유지 시간 초과로 종료시 error=com.launchdarkly.eventsource.StreamClosedByServerException: Stream closed by server 예외가 발생
                Timber.d("SSE onError: $t")
            }
        }

    fun connect(): Flow<SseResponse> {
        sseClient.connect(eventHandler)
        return eventFlow
    }

    fun disconnect() {
        sseClient.disconnect()
    }
}
