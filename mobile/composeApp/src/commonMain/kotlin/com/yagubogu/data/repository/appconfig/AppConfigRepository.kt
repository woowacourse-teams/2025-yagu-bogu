package com.yagubogu.data.repository.appconfig

interface AppConfigRepository {
    suspend fun fetchConfigs()

    fun isMaintenanceMode(): Boolean

    fun getMaintenanceMessage(): String
}
