package com.yagubogu.data.local

import java.util.UUID

internal actual fun generateDeviceId(): String = UUID.randomUUID().toString()
