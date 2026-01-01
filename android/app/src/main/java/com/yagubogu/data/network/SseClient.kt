package com.yagubogu.data.network

import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.stream.SseStreamResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import timber.log.Timber

class SseClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val json: Json,
) {
    fun connect(): Flow<SseStreamResponse> =
        flow {
            try {
                httpClient.sse("$baseUrl$EVENT_STREAM_ENDPOINT") {
                    Timber.d("SSE: Connection Opened")
                    emit(SseStreamResponse.ConnectionOpened)

                    incoming.collect { serverSentEvent: ServerSentEvent ->
                        val eventType: String? = serverSentEvent.event
                        val rawData: String = serverSentEvent.data ?: ""

                        val response: SseStreamResponse =
                            when (eventType) {
                                EVENT_CHECK_IN_CREATED -> {
                                    try {
                                        val items: List<FanRateByGameDto> =
                                            json.decodeFromString(rawData)
                                        SseStreamResponse.CheckInCreated(items)
                                    } catch (e: Exception) {
                                        SseStreamResponse.Error(e)
                                    }
                                }

                                EVENT_CONNECT -> SseStreamResponse.Connect(rawData)
                                EVENT_TIMEOUT -> SseStreamResponse.Timeout(rawData)
                                else -> SseStreamResponse.Comment(serverSentEvent.comments ?: "")
                            }
                        Timber.d("SSE: $response")
                        emit(response)
                    }
                }
            } catch (e: Exception) {
                Timber.d("SSE error: $e")
                emit(SseStreamResponse.Error(e))
            } finally {
                Timber.d("SSE: Connection Closed")
                emit(SseStreamResponse.ConnectionClosed)
            }
        }

    companion object {
        private const val EVENT_STREAM_ENDPOINT = "api/v1/event-stream"

        private const val EVENT_CHECK_IN_CREATED = "check-in-created"
        private const val EVENT_CONNECT = "connect"
        private const val EVENT_TIMEOUT = "timeout"
    }
}
