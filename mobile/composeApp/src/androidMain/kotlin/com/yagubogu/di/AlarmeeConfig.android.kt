package com.yagubogu.di

import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.yagubogu.R
import com.yagubogu.data.notification.NotificationChannels

actual fun provideAlarmeeConfig(): AlarmeePlatformConfiguration =
    AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_yagubogu_notification,
        notificationChannels =
            NotificationChannels.allChannels.map { info ->
                AlarmeeNotificationChannel(
                    id = info.id,
                    name = info.name,
                    importance = info.importance,
                )
            },
    )
