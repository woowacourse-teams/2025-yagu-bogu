package com.yagubogu.analytics

interface Analytics {
    fun logEvent(
        event: String,
        params: Map<String, Any> = emptyMap(),
    )
}
