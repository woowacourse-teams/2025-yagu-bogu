package com.yagubogu.ui.setting

import androidx.compose.runtime.Composable

interface ScoreWidgetNotificationPermissionManager {
    fun isNotificationEnabled(): Boolean

    fun requestPermission()

    fun openNotificationSettings()

    fun cancelNotification()
}

@Composable
expect fun rememberScoreWidgetNotificationPermissionManager(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
): ScoreWidgetNotificationPermissionManager
