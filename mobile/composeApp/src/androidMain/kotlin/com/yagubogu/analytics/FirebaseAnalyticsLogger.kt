package com.yagubogu.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

class FirebaseAnalyticsLogger : Analytics {
    override fun logEvent(
        event: String,
        params: Map<String, Any>,
    ) {
        Firebase.analytics.logEvent(event) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Int -> param(key, value.toLong())
                }
            }
        }
    }
}
