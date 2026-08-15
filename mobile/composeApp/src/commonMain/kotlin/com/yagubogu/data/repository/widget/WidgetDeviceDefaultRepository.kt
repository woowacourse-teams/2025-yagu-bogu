package com.yagubogu.data.repository.widget

import com.yagubogu.BuildKonfig
import com.yagubogu.data.datasource.widget.WidgetDeviceDataSource
import com.yagubogu.data.local.ScoreWidgetDeviceId

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
}
