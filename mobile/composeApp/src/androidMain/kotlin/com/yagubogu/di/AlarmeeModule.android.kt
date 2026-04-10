package com.yagubogu.di

import android.app.NotificationManager
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.yagubogu.R

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration =
    AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_yagubogu_notification,
        notificationChannels =
            listOf(
                AlarmeeNotificationChannel(
                    id = "geofenceChannelId",
                    name = "경기장 근처 직관 인증 리마인더 알림",
                    importance = NotificationManager.IMPORTANCE_HIGH,
                ),
            ),
    )
