package com.yagubogu.data.repository.appconfig

import com.yagubogu.data.datasource.appconfig.AppConfigRemoteDataSource

class AppConfigDefaultRepository(
    private val dataSource: AppConfigRemoteDataSource, // ← Firebase 직접 의존 없음
) : AppConfigRepository {
    override suspend fun fetchConfigs() {
        dataSource.fetchAndActivate()
    }

    override fun isMaintenanceMode(): Boolean = dataSource.getBoolean("is_maintenance")

    override fun getMaintenanceMessage(): String = dataSource.getString("maintenance_message")
}
