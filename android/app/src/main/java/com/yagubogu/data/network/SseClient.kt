package com.yagubogu.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SseClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
) {
    private var sseJob: Job? = null
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun connect(sseHandler: SseHandler) {
        if (sseJob?.isActive == true) return

        sseJob =
            scope.launch {
                try {
                    httpClient.sse(urlString = "$baseUrl$EVENT_STREAM_ENDPOINT") {
                        sseHandler.onConnectionOpened()

                        incoming.collect { serverSentEvent: ServerSentEvent ->
                            val event: String? = serverSentEvent.event
                            val data: String? = serverSentEvent.data

                            if (event == null || data == null) {
                                sseHandler.onComment(serverSentEvent.comments ?: "")
                            } else {
                                sseHandler.onEventReceived(event, data)
                            }
                        }
                    }
                } catch (exception: Exception) {
                    // CancellationException은 의도된 종료(disconnect)이므로 에러로 처리 안 함
                    if (exception !is CancellationException) {
                        sseHandler.onError(exception)
                    }
                } finally {
                    sseHandler.onConnectionClosed()
                }
            }
    }

    fun disconnect() {
        sseJob?.cancel()
        sseJob = null
    }

    companion object {
        private const val EVENT_STREAM_ENDPOINT = "api/v1/event-stream"
    }
}
