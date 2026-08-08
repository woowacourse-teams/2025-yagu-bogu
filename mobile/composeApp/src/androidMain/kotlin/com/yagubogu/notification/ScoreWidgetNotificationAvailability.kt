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
    fun isEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val channel =
            context
                .getSystemService<NotificationManager>()
                ?.getNotificationChannel(ScoreWidgetNotificationChannel.ID)

        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            channel?.importance != NotificationManager.IMPORTANCE_NONE
    }
}
