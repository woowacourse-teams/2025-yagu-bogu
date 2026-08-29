package com.yagubogu.notification

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScoreWidgetFirebaseMessagingService :
    FirebaseMessagingService(),
    KoinComponent {
    private val logger = Logger.withTag("ScoreWidgetFcm")
    private val scoreWidgetMessageProcessor: ScoreWidgetMessageProcessor by inject()
    private val scoreWidgetDeviceRegistrar: ScoreWidgetDeviceRegistrar by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        val result =
            runBlocking(Dispatchers.IO) {
                scoreWidgetMessageProcessor.process(message.data)
            }
        logger.i {
            "실시간 스코어 위젯 FCM 메시지 처리 결과: $result"
        }
    }

    override fun onNewToken(token: String) {
        logger.i { "실시간 스코어 위젯 등록용 FCM 토큰이 갱신되어 디바이스 등록을 진행합니다." }
        runBlocking(Dispatchers.IO) {
            scoreWidgetDeviceRegistrar.register(token)
        }
    }
}
