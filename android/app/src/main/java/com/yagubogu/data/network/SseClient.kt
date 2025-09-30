package com.yagubogu.data.network

import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.launchdarkly.eventsource.background.BackgroundEventSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.net.URI

class SseClient(
    private val baseUrl: String,
    private val httpClient: OkHttpClient,
) {
    private var eventSource: BackgroundEventSource? = null

    fun connect(sseHandler: SseHandler) {
        if (eventSource == null) {
            val eventHandler: BackgroundEventHandler = SseEventHandler(sseHandler)
            eventSource =
                BackgroundEventSource.Builder(eventHandler, getEventSource()).build()
            eventSource?.start()
        }
    }

    fun disconnect() {
        eventSource?.let {
            CoroutineScope(Dispatchers.IO).launch {
                it.close()
                eventSource = null
            }
        }
    }

    private fun getEventSource(): EventSource.Builder =
        EventSource
            .Builder(
                ConnectStrategy
                    .http(URI.create("$baseUrl$EVENT_STREAM_ENDPOINT"))
                    .httpClient(httpClient)
                    .header("Accept", "text/event-stream"),
            )

    private class SseEventHandler(
        private val sseHandler: SseHandler,
    ) : BackgroundEventHandler {
        // SSE 연결 성공
        override fun onOpen() {
            sseHandler.onConnectionOpened()
        }

        // SSE 연결 종료
        override fun onClosed() {
            sseHandler.onConnectionClosed()
        }

        // SSE 이벤트 도착
        override fun onMessage(
            event: String,
            messageEvent: MessageEvent,
        ) {
            sseHandler.onEventReceived(event, messageEvent)
        }

        // SSE 연결 오류 발생
        override fun onError(t: Throwable) {
            sseHandler.onError(t)
        }

        override fun onComment(comment: String) {
            sseHandler.onComment(comment)
        }
    }

    companion object {
        private const val EVENT_STREAM_ENDPOINT = "/api/event-stream"
    }
}
