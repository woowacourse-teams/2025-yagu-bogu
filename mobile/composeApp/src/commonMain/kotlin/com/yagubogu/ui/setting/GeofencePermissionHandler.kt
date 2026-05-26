package com.yagubogu.ui.setting

import com.yagubogu.ui.common.platform.PlatformType
import com.yagubogu.ui.common.platform.androidVersion
import com.yagubogu.ui.common.platform.currentPlatform
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.BACKGROUND_LOCATION
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GeofencePermissionHandler(
    private val permissionController: PermissionsController,
    private val updateGeofenceNotification: (Boolean) -> Unit,
    private val onShowLocationDialog: () -> Unit,
    private val onShowNotificationDialog: () -> Unit,
    private val scope: CoroutineScope,
) {
    fun handleToggle(enable: Boolean) {
        if (!enable) {
            updateGeofenceNotification(false)
            return
        }

        scope.launch {
            if (!checkNotificationPermission()) return@launch
            if (!checkLocationPermission()) return@launch
            updateGeofenceNotification(true)
        }
    }

    private suspend fun checkNotificationPermission(): Boolean {
        if (permissionController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)) return true

        // 안드로이드 13 이상 런타임 알림 권한 지원
        val supportsRuntime =
            when (currentPlatform) {
                PlatformType.ANDROID -> androidVersion >= AndroidApiLevel.TIRAMISU_13
                PlatformType.IOS -> true
            }

        return when (supportsRuntime) {
            true -> {
                runCatching {
                    permissionController.providePermission(Permission.REMOTE_NOTIFICATION)
                }.fold(
                    onSuccess = { true },
                    onFailure = { throwable ->
                        if (throwable is DeniedAlwaysException) {
                            onShowNotificationDialog()
                        }
                        false
                    },
                )
            }
            false -> {
                onShowNotificationDialog()
                false
            }
        }
    }

    private suspend fun checkLocationPermission(): Boolean {
        if (permissionController.isPermissionGranted(Permission.BACKGROUND_LOCATION)) return true

        handlePlatformLocationPermission()
        return false
    }

    private suspend fun handlePlatformLocationPermission() {
        when (currentPlatform) {
            PlatformType.IOS -> {
                try {
                    // iOS: 항상 허용 항목을 설정에 노출시키려면 먼저 포그라운드 위치 권한 요청 필요
                    if (!permissionController.isPermissionGranted(Permission.LOCATION)) {
                        permissionController.providePermission(Permission.LOCATION)
                        delay(IOS_LOCATION_PERMISSION_REQUEST_DELAY)
                    }
                } catch (e: Exception) {
                    // 권한 거부 시나리오는 하단 다이얼로그에서 처리
                }
                onShowLocationDialog()
            }

            PlatformType.ANDROID -> {
                when (androidVersion) {
                    AndroidApiLevel.Q_10 -> {
                        runCatching {
                            permissionController.providePermission(Permission.BACKGROUND_LOCATION)
                        }.onSuccess {
                            updateGeofenceNotification(true)
                        }.onFailure {
                            onShowLocationDialog()
                        }
                    }
                    else -> { // Android 11+
                        // 안드로이드 11부터는 백그라운드 위치 권한을 무조건 설정 페이지에서 사용자가 직접 항상 허용을 선택해야 함
                        onShowLocationDialog()
                    }
                }
            }
        }
    }

    companion object {
        private const val IOS_LOCATION_PERMISSION_REQUEST_DELAY = 500L

        private object AndroidApiLevel {
            const val TIRAMISU_13 = 33
            const val Q_10 = 29
        }
    }
}
