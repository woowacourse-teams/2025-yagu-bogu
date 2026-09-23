package com.yagubogu.ui.setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.yagubogu.notification.ScoreWidgetNotificationAvailability
import com.yagubogu.notification.ScoreWidgetNotificationManager

@Composable
actual fun rememberScoreWidgetNotificationPermissionManager(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
): ScoreWidgetNotificationPermissionManager {
    val context = LocalContext.current
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) onPermissionGranted() else onPermissionDenied()
        }

    return remember(context, onPermissionGranted, onPermissionDenied) {
        object : ScoreWidgetNotificationPermissionManager {
            override fun isNotificationEnabled(): Boolean = ScoreWidgetNotificationAvailability(context).isEnabled()

            override fun requestPermission() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onPermissionDenied()
                }
            }

            override fun openNotificationSettings() {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                )
            }

            override fun cancelNotification() {
                ScoreWidgetNotificationManager.cancel(context)
            }
        }
    }
}
