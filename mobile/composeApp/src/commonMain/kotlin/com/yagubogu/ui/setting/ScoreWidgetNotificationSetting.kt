package com.yagubogu.ui.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.yagubogu.ui.setting.component.SettingToggleButton
import com.yagubogu.ui.setting.component.dialog.ScoreWidgetPermissionDialog
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_score_widget_notification

@Composable
fun ScoreWidgetNotificationSetting(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
    val permissionManager =
        rememberScoreWidgetNotificationPermissionManager(
            onPermissionGranted = { onEnabledChange(true) },
            onPermissionDenied = { showPermissionDialog = true },
        )

    fun updateEnabled(enabled: Boolean) {
        if (!enabled) permissionManager.cancelNotification()
        onEnabledChange(enabled)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (enabled && !permissionManager.isNotificationEnabled()) {
            updateEnabled(false)
        }
    }

    SettingToggleButton(
        text = stringResource(Res.string.setting_score_widget_notification),
        checked = enabled,
        onCheckedChange = { shouldEnable ->
            if (!shouldEnable) {
                updateEnabled(false)
            } else if (permissionManager.isNotificationEnabled()) {
                updateEnabled(true)
            } else {
                permissionManager.requestPermission()
            }
        },
    )

    if (showPermissionDialog) {
        ScoreWidgetPermissionDialog(
            onConfirm = {
                showPermissionDialog = false
                permissionManager.openNotificationSettings()
            },
            onCancel = { showPermissionDialog = false },
        )
    }
}
