package com.yagubogu.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun rememberOpenNotificationSettings(fallback: () -> Unit): () -> Unit =
    remember {
        {
            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
            if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(
                    url = url,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = null,
                )
            }
        }
    }
