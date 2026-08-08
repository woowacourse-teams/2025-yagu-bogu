package com.yagubogu.notification

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ScoreWidgetFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = Logger.withTag("ScoreWidgetFcm")

    override fun onMessageReceived(message: RemoteMessage) {
        val payload = ScoreWidgetPayload.from(message.data)
        if (payload == null) {
            logger.w { "Ignoring an invalid score widget payload." }
            return
        }

        val result =
            runBlocking(Dispatchers.IO) {
                ScoreWidgetNotificationManager(applicationContext).handle(payload)
            }
        logger.i {
            "Score widget ${payload.type} gameId=${payload.gameId} revision=${payload.displayRevision}: $result"
        }
    }

    override fun onNewToken(token: String) {
        logger.i { "FCM token refreshed for score widget registration." }
        // Widget device registration is added when the backend API is available.
    }
}
