package com.yagubogu.data.repository.widget

import com.yagubogu.BuildKonfig
import com.yagubogu.data.datasource.widget.WidgetDeviceDataSource
import com.yagubogu.data.local.ScoreWidgetDeviceId
import com.yagubogu.data.util.ApiException

class WidgetDeviceDefaultRepository(
    private val deviceIdStore: ScoreWidgetDeviceId,
    private val deviceDataSource: WidgetDeviceDataSource,
) : WidgetDeviceRepository {
    private val appVersion: String =
        with(BuildKonfig.VERSION_CODE) {
            "${this / 10000}.${(this % 10000) / 100}.${this % 100}"
        }

    override suspend fun registerDevice(pushToken: String): Result<Unit> =
        deviceDataSource.registerDevice(
            deviceId = deviceIdStore.get(),
            pushToken = pushToken,
            appVersion = appVersion,
        )

    override suspend fun deregisterDevice(): Result<Unit> =
        deviceDataSource
            .deregisterDevice(deviceId = deviceIdStore.get())
            .recover { exception: Throwable ->
                // 이미 해제되어 있거나(404) 로그인이 만료된(401) 상태도 정상으로 취급한다.
                when (exception) {
                    is ApiException.NotFound,
                    is ApiException.Unauthorized,
                    -> Result.success(Unit)

                    else -> Result.failure(exception)
                }
            }.map { }
}
