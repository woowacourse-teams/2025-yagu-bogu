package com.yagubogu.ui.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberScoreWidgetNotificationPermissionManager(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
): ScoreWidgetNotificationPermissionManager =
    remember {
        object : ScoreWidgetNotificationPermissionManager {
            override fun isNotificationEnabled(): Boolean = false

            override fun requestPermission() = Unit

            override fun openNotificationSettings() = Unit

            override fun cancelNotification() = Unit
        }
    }
