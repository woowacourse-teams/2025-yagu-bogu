package com.yagubogu.data.network

interface SseHandler {
    fun onConnectionOpened()

    fun onConnectionClosed()

    fun onEventReceived(
        event: String,
        data: String,
    )

    fun onComment(comment: String)

    fun onError(t: Throwable)
}
