package com.yagubogu.notification

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

class ScoreWidgetNotificationAvailability(
    private val context: Context,
) {
    /**
     * 알림 표시 가능 여부(권한 + 채널 + 앱 설정)를 확인한다.
     * [notify] 호출 사이드에서는 권한 검사가 정적 분석에 보이도록
     * 호출부에서 [hasNotificationPermission]을 직접 인라인으로 가드한다.
     */
    fun isEnabled(): Boolean = hasNotificationPermission() && isChannelEnabled()

    fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    fun isChannelEnabled(): Boolean {
        val channel =
            context
                .getSystemService<NotificationManager>()
                ?.getNotificationChannel(ScoreWidgetNotificationChannel.ID)

        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            channel != null &&
            channel.importance != NotificationManager.IMPORTANCE_NONE
    }
}
