package com.yagubogu.data.datasource.widget

interface WidgetDeviceDataSource {
    suspend fun registerDevice(
        deviceId: String,
        pushToken: String,
        appVersion: String?,
    ): Result<Unit>

    suspend fun deregisterDevice(deviceId: String): Result<Unit>
}
