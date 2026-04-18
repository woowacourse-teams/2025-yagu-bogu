package com.yagubogu.data.service

import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import com.yagubogu.domain.service.NotificationService
import org.jetbrains.compose.resources.getString
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.notification_geofence_body
import yagubogu.composeapp.generated.resources.notification_geofence_title

class NotificationDefaultService(
    private val alarmeeService: AlarmeeService,
) : NotificationService {
    override suspend fun sendStadiumEntryNotification(
        stadiumName: String,
        stadiumId: Int,
    ) {
        val title = getString(Res.string.notification_geofence_title, stadiumName)
        val body = getString(Res.string.notification_geofence_body)

        alarmeeService.local.immediate(
            alarmee =
                Alarmee(
                    uuid = "enter_$stadiumId",
                    notificationTitle = title,
                    notificationBody = body,
                    androidNotificationConfiguration =
                        AndroidNotificationConfiguration(
                            priority = AndroidNotificationPriority.HIGH,
                            channelId = "geofenceChannelId",
                        ),
                    iosNotificationConfiguration = IosNotificationConfiguration(),
                ),
        )
    }
}
