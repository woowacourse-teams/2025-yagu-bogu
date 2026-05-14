package com.yagubogu.data.datasource.appconfig

import com.yagubogu.data.dto.response.appconfig.HomeNoticeResponse
import com.yagubogu.data.dto.response.appconfig.MaintenanceResponse

interface AppConfigRemoteDataSource {
    suspend fun fetchAndActivate()

    fun getBoolean(key: String): Boolean

    fun getString(key: String): String

    fun getMaintenanceResponse(): MaintenanceResponse

    fun getHomeNoticeResponse(): HomeNoticeResponse
}
