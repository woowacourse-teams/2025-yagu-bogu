package com.yagubogu.data.repository.widget

interface WidgetDeviceRepository {
    suspend fun registerDevice(pushToken: String): Result<Unit>
}
