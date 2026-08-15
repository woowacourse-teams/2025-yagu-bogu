package com.yagubogu.notification

import co.touchlab.kermit.Logger
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.yagubogu.data.network.TokenManager
import com.yagubogu.data.repository.widget.WidgetDeviceRegistrar
import com.yagubogu.data.repository.widget.WidgetDeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScoreWidgetDeviceRegistrar(
    private val widgetDeviceRepository: WidgetDeviceRepository,
    private val tokenManager: TokenManager,
) : WidgetDeviceRegistrar {
    private val logger = Logger.withTag("ScoreWidgetDeviceRegistrar")

    /** 로그인 상태를 관찰해 디바이스 등록을 수행한다. 스코프는 호출자가 관리한다. */
    fun start(scope: CoroutineScope) {
        scope.launch {
            tokenManager.accessTokenFlow
                .distinctUntilChanged()
                .filterNotNull()
                .collect { register(getFcmToken()) }
        }
    }

    /**
     * 현재 FCM 토큰으로 디바이스 등록을 보장한다.
     * 토큰이 없거나 로그인 상태가 아니면 실패를 반환한다.
     */
    override suspend fun register(): Result<Unit> {
        val pushToken: String =
            getFcmToken()
                ?: return Result.failure(IllegalStateException("FCM 토큰을 가져올 수 없습니다"))

        if (tokenManager.getAccessToken() == null) {
            return Result.failure(IllegalStateException("로그인 상태가 아닙니다"))
        }

        return registerDevice(pushToken)
    }

    suspend fun register(pushToken: String?) {
        if (pushToken == null) {
            logger.w { "FCM 토큰을 가져올 수 없어 디바이스 등록을 건너뜁니다." }
            return
        }
        if (tokenManager.getAccessToken() == null) {
            logger.i { "로그인 상태가 아니어서 디바이스 등록을 건너뜁니다." }
            return
        }
        registerDevice(pushToken)
    }

    private suspend fun registerDevice(pushToken: String): Result<Unit> =
        widgetDeviceRepository
            .registerDevice(pushToken)
            .onSuccess {
                logger.i { "실시간 스코어 위젯 디바이스 등록에 성공했습니다." }
            }.onFailure { exception: Throwable ->
                logger.w(exception) { "실시간 스코어 위젯 디바이스 등록에 실패했습니다." }
            }

    private suspend fun getFcmToken(): String? =
        withContext(Dispatchers.IO) {
            runCatching { Tasks.await(FirebaseMessaging.getInstance().token) }.getOrNull()
        }
}
