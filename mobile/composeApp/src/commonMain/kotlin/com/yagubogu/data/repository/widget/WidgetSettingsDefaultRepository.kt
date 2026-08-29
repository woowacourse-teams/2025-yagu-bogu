package com.yagubogu.data.repository.widget

import co.touchlab.kermit.Logger
import com.yagubogu.data.datasource.widget.WidgetSettingsDataSource
import com.yagubogu.data.local.ScoreWidgetSettings
import com.yagubogu.data.network.TokenManager
import kotlinx.coroutines.flow.Flow

class WidgetSettingsDefaultRepository(
    private val localSettings: ScoreWidgetSettings,
    private val dataSource: WidgetSettingsDataSource,
    private val tokenManager: TokenManager,
) : WidgetSettingsRepository {
    private val logger = Logger.withTag("WidgetSettingsRepository")

    override val enabled: Flow<Boolean> = localSettings.isEnabled

    override suspend fun refresh(): Result<Unit> {
        if (!isLoggedIn()) return Result.success(Unit)

        return dataSource
            .getSetting()
            .onSuccess { response ->
                localSettings.setEnabled(response.enabled)
            }.onFailure { exception: Throwable ->
                logger.w(exception) { "위젯 사용 설정 동기화 실패" }
            }.map { }
    }

    override suspend fun setEnabled(enabled: Boolean): Result<Unit> {
        if (!isLoggedIn()) {
            localSettings.setEnabled(enabled)
            return Result.success(Unit)
        }

        return dataSource
            .patchSetting(enabled)
            .onSuccess { response ->
                localSettings.setEnabled(response.enabled)
            }.onFailure { exception: Throwable ->
                logger.w(exception) { "위젯 사용 설정 변경 실패" }
            }.map { }
    }

    private suspend fun isLoggedIn(): Boolean = tokenManager.getAccessToken() != null
}
