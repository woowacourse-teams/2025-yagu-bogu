package com.yagubogu.data.datasource.appconfig

import kotlinx.coroutines.flow.Flow

interface AppConfigLocalDataSource {
    val maintenanceIgnoreInfo: Flow<IgnoreInfo>

    val homeNoticeIgnoreInfo: Flow<IgnoreInfo>

    suspend fun saveMaintenanceIgnoreInfo(
        id: Int,
        expiryTime: Long,
    )

    suspend fun saveHomeNoticeIgnoreInfo(
        id: Int,
        expiryTime: Long,
    )
}
