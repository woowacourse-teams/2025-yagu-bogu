package com.yagubogu.data.notification

import android.app.NotificationManager

data class NotificationChannelInfo(
    val id: String,
    val name: String,
    val importance: Int = NotificationManager.IMPORTANCE_HIGH,
)
