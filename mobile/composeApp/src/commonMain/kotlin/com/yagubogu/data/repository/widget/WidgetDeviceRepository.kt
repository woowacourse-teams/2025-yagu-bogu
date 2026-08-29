package com.yagubogu.data.repository.widget

interface WidgetDeviceRepository {
    suspend fun registerDevice(pushToken: String): Result<Unit>

    /** 현재 기기의 디바이스 등록을 해제한다. */
    suspend fun deregisterDevice(): Result<Unit>
}
