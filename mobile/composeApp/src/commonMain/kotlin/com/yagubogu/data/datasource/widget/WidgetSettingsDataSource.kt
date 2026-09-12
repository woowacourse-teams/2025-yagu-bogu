package com.yagubogu.data.datasource.widget

import com.yagubogu.data.dto.response.widget.WidgetSettingResponse

/**
 * 위젯 사용 설정 원격 저장소 계약.
 *
 * 현재는 계정 단위(`/api/v1/widgets/settings`) 구현을 사용한다.
 * 기기 단위(`/api/v1/widgets/devices/{deviceId}/settings`)로 계약이 확정되면
 * 이 인터페이스를 구현하는 구현체를 새로 만들고 DI 바인딩만 교체하면 된다.
 */
interface WidgetSettingsDataSource {
    suspend fun getSetting(): Result<WidgetSettingResponse>

    suspend fun patchSetting(enabled: Boolean): Result<WidgetSettingResponse>
}
