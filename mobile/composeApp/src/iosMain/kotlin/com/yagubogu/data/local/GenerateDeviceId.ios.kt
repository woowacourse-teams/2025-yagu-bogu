package com.yagubogu.data.local

import platform.Foundation.NSUUID

internal actual fun generateDeviceId(): String = NSUUID().UUIDString.lowercase()
