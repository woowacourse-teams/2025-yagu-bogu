package com.yagubogu.analytics

object AnalyticsLogger {
    private var delegate: Analytics? = null

    fun initialize(analytics: Analytics) {
        delegate = analytics
    }

    fun logEvent(
        event: String,
        params: Map<String, Any> = emptyMap(),
    ) {
        delegate?.logEvent(event, params)
    }
}
