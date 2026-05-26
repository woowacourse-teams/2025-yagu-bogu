package com.yagubogu.ui.common.platform

actual val currentPlatform: PlatformType = PlatformType.ANDROID

actual val androidVersion: Int = android.os.Build.VERSION.SDK_INT
