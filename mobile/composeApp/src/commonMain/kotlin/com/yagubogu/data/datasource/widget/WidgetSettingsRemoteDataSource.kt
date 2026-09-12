package com.yagubogu.data.datasource.widget

import com.yagubogu.data.dto.request.widget.WidgetSettingRequest
import com.yagubogu.data.dto.response.widget.WidgetSettingResponse
import com.yagubogu.data.service.WidgetApiService
import com.yagubogu.data.util.safeApiCall

class WidgetSettingsRemoteDataSource(
    private val widgetApiService: WidgetApiService,
) : WidgetSettingsDataSource {
    override suspend fun getSetting(): Result<WidgetSettingResponse> =
        safeApiCall {
            widgetApiService.getSetting()
        }

    override suspend fun patchSetting(enabled: Boolean): Result<WidgetSettingResponse> =
        safeApiCall {
            widgetApiService.patchSetting(WidgetSettingRequest(enabled = enabled))
        }
}
