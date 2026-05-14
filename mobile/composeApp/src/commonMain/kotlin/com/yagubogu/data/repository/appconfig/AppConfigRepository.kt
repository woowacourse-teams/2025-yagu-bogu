package com.yagubogu.data.repository.appconfig

import com.yagubogu.ui.home.model.HomeNoticeInfo
import com.yagubogu.ui.home.model.MaintenanceInfo

interface AppConfigRepository {
    suspend fun fetchConfigs()

    suspend fun getMaintenanceInfo(): MaintenanceInfo

    suspend fun getHomeNoticeInfo(): HomeNoticeInfo

    suspend fun markMaintenanceDialogAsIgnored(
        maintenanceId: Int,
        days: Int,
    )

    suspend fun markHomeNoticeDialogAsIgnored(
        homeNoticeId: Int,
        days: Int,
    )

    fun isPastCheckInAdEnabled(): Boolean
}
