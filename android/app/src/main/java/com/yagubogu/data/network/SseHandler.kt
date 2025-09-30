package com.yagubogu.data.network

import com.launchdarkly.eventsource.MessageEvent

interface SseHandler {
    fun onConnectionOpened()

    fun onConnectionClosed()

    fun onEventReceived(
        event: String,
        messageEvent: MessageEvent,
    )

    fun onComment(comment: String)

    fun onError(t: Throwable)
}
