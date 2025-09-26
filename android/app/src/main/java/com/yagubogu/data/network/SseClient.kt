package com.yagubogu.data.network

import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.launchdarkly.eventsource.background.BackgroundEventSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.util.concurrent.TimeUnit

class SseClient(
    private val baseUrl: String,
    private val tokenManager: TokenManager,
) {
    private var backgroundEventSource: BackgroundEventSource? = null

    fun connect(eventHandler: BackgroundEventHandler) {
        if (backgroundEventSource == null) {
            backgroundEventSource =
                BackgroundEventSource.Builder(eventHandler, getEventSource()).build()
            backgroundEventSource?.start()
        }
    }

    fun disconnect() {
        backgroundEventSource?.let {
            CoroutineScope(Dispatchers.IO).launch {
                it.close()
                backgroundEventSource = null
            }
        }
    }

    private fun getEventSource(): EventSource.Builder {
        val path = "/api/event-stream" // SSE endpoint path
        val accessToken: String? = runBlocking { tokenManager.getAccessToken() }

        return EventSource
            .Builder(
                ConnectStrategy
                    .http(URI.create("$baseUrl$path"))
                    .header("Authorization", "Bearer $accessToken")
                    .header("Accept", "text/event-stream")
                    .connectTimeout(0, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS),
            )
    }
}
