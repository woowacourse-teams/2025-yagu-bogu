package com.yagubogu.notification

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yagubogu.data.local.ScoreWidgetSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScoreWidgetFirebaseMessagingService :
    FirebaseMessagingService(),
    KoinComponent {
    private val logger = Logger.withTag("ScoreWidgetFcm")
    private val scoreWidgetSettings: ScoreWidgetSettings by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        val isEnabled =
            runBlocking(Dispatchers.IO) {
                scoreWidgetSettings.isEnabled.first()
            }
        if (!isEnabled) {
            logger.i { "실시간 스코어 위젯이 비활성화되어 FCM 메시지를 무시합니다." }
            return
        }

        val payload = ScoreWidgetPayload.from(message.data)
        if (payload == null) {
            logger.w { "유효하지 않은 실시간 스코어 위젯 payload를 무시합니다." }
            return
        }

        val result =
            runBlocking(Dispatchers.IO) {
                ScoreWidgetNotificationManager(applicationContext).handle(payload)
            }
        logger.i {
            "실시간 스코어 위젯 ${payload.type} 처리: gameId=${payload.gameId}, revision=${payload.displayRevision}, 결과=$result"
        }
    }

    override fun onNewToken(token: String) {
        logger.i { "실시간 스코어 위젯 등록용 FCM 토큰이 갱신되었습니다." }
        // Widget device registration is added when the backend API is available.
    }
}
