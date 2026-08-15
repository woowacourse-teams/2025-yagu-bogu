package com.yagubogu.data.datasource.widget

import com.yagubogu.data.dto.request.widget.WidgetDeviceRequest
import com.yagubogu.data.service.WidgetApiService
import com.yagubogu.data.util.safeApiCall

class WidgetDeviceRemoteDataSource(
    private val widgetApiService: WidgetApiService,
) : WidgetDeviceDataSource {
    override suspend fun registerDevice(
        deviceId: String,
        pushToken: String,
        appVersion: String?,
    ): Result<Unit> =
        safeApiCall {
            widgetApiService.postDevice(
                WidgetDeviceRequest(
                    platform = PLATFORM_ANDROID,
                    deviceId = deviceId,
                    pushToken = pushToken,
                    appVersion = appVersion,
                ),
            )
        }

    private companion object {
        const val PLATFORM_ANDROID = "ANDROID"
    }
}
