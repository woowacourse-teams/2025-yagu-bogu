package com.yagubogu.data.notification

object NotificationChannels {
    const val GEOFENCE_CHANNEL_ID = "geofenceChannelId"
    val allChannels =
        listOf(
            NotificationChannelInfo(
                id = GEOFENCE_CHANNEL_ID,
                name = "경기장 근처 직관 인증 리마인더 알림",
            ),
        )
}
