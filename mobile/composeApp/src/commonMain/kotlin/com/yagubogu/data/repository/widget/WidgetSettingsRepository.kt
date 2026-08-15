package com.yagubogu.data.repository.widget

import kotlinx.coroutines.flow.Flow

/**
 * 위젯 사용 설정 저장소 계약.
 * UI와 알림 처리 로직은 이 인터페이스에만 의존한다.
 */
interface WidgetSettingsRepository {
    /** 현재 위젯 사용 여부. 로컬 기준이며 서버 동기화 결과가 반영된 값. */
    val enabled: Flow<Boolean>

    /** 서버에 있는 설정을 로컬로 동기화한다. 비로그인 상태는 no-op. */
    suspend fun refresh(): Result<Unit>

    /** 위젯 사용 여부를 변경한다. 로그인 상태에서는 서버에 반영한 뒤 로컬에 적용한다. */
    suspend fun setEnabled(enabled: Boolean): Result<Unit>
}
