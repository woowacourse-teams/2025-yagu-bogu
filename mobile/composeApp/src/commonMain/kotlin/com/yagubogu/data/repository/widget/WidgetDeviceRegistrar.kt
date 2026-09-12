package com.yagubogu.data.repository.widget

/**
 * 현재 기기를 FCM 토큰으로 백엔드에 등록한다.
 *
 * 설정 토글이 켜지기 전에 선행 등록을 보장해, "토글은 켜져 있는데
 * 실제로는 푸시를 받을 수 없는" 상태가 만들어지지 않도록 한다.
 */
interface WidgetDeviceRegistrar {
    suspend fun register(): Result<Unit>
}
