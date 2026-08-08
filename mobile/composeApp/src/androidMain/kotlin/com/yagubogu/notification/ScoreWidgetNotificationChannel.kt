package com.yagubogu.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.yagubogu.R

object ScoreWidgetNotificationChannel {
    const val ID = "score_widget"

    fun create(context: Context) {
        val channel =
            NotificationChannel(
                ID,
                context.getString(R.string.score_widget_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.score_widget_channel_description)
                setSound(null, null)
                enableVibration(false)
            }

        context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }
}
