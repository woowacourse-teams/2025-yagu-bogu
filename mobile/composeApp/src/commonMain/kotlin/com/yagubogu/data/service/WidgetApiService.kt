package com.yagubogu.data.service

import com.yagubogu.data.dto.request.widget.WidgetDeviceRequest
import com.yagubogu.data.dto.request.widget.WidgetSettingRequest
import com.yagubogu.data.dto.response.widget.WidgetSettingResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path

/**
 * 잠금화면 위젯 API.
 *
 * 현재는 계정 단위([GET/PATCH] /api/v1/widgets/settings) 계약만 사용한다.
 * 기기 단위(`/api/v1/widgets/devices/{deviceId}/settings`)로 계약이 바뀌어도
 * request/response body는 동일하므로, 교체는 [com.yagubogu.data.datasource.widget.WidgetSettingsDataSource]
 * 구현체 수준에서만 일어난다.
 */
interface WidgetApiService {
    @GET("/api/v1/widgets/settings")
    suspend fun getSetting(): WidgetSettingResponse

    @PATCH("/api/v1/widgets/settings")
    suspend fun patchSetting(
        @Body body: WidgetSettingRequest,
    ): WidgetSettingResponse

    @POST("/api/v1/widgets/devices")
    suspend fun postDevice(
        @Body body: WidgetDeviceRequest,
    )

    @DELETE("/api/v1/widgets/devices/{deviceId}")
    suspend fun deleteDevice(
        @Path("deviceId") deviceId: String,
    )
}
