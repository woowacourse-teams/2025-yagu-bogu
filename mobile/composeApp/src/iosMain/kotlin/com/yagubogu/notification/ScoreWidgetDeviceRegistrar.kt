package com.yagubogu.notification

import com.yagubogu.data.repository.widget.WidgetDeviceRegistrar

/** iOS는 실시간 스코어 위젯이 Android 전용이므로 등록을 성공으로 간주한다. */
class ScoreWidgetDeviceRegistrar : WidgetDeviceRegistrar {
    override suspend fun register(): Result<Unit> = Result.success(Unit)
}
