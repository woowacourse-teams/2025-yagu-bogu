package com.yagubogu.data.repository.appconfig

import com.yagubogu.data.datasource.appconfig.AppConfigLocalDataSource
import com.yagubogu.data.datasource.appconfig.AppConfigRemoteDataSource
import com.yagubogu.ui.home.model.HomeNoticeInfo
import com.yagubogu.ui.home.model.MaintenanceInfo
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

class AppConfigDefaultRepository(
    private val remoteDataSource: AppConfigRemoteDataSource,
    private val localDataSource: AppConfigLocalDataSource,
    private val clock: Clock,
) : AppConfigRepository {
    override suspend fun fetchConfigs() {
        remoteDataSource.fetchAndActivate()
    }

    override suspend fun getMaintenanceInfo(): MaintenanceInfo {
        val response = remoteDataSource.getMaintenanceResponse()
        val ignoreInfo = localDataSource.maintenanceIgnoreInfo.first()
        val shouldShow =
            when {
                response.id == DEFAULT_CONFIG_ID -> true
                else ->
                    response.isShow &&
                        ignoreInfo.shouldShow(response.id, clock.now().toEpochMilliseconds())
            }

        return MaintenanceInfo(
            id = response.id,
            remoteIsShow = response.isShow,
            shouldShowPopup = shouldShow,
            emoji = response.emoji,
            title = response.title,
            message = response.message,
            textAlign = response.textAlign,
            skippableDays = response.skippableDays,
            isLoginBlock = response.isLoginBlock,
        )
    }

    override suspend fun getHomeNoticeInfo(): HomeNoticeInfo {
        val response = remoteDataSource.getHomeNoticeResponse()
        val ignoreInfo = localDataSource.homeNoticeIgnoreInfo.first()
        val shouldShow =
            response.isShow &&
                ignoreInfo.shouldShow(response.id, clock.now().toEpochMilliseconds())
        return HomeNoticeInfo(
            id = response.id,
            remoteIsShow = response.isShow,
            shouldShowPopup = shouldShow,
            emoji = response.emoji,
            title = response.title,
            message = response.message,
            textAlign = response.textAlign,
            skippableDays = response.skippableDays,
        )
    }

    override suspend fun markMaintenanceDialogAsIgnored(
        maintenanceId: Int,
        days: Int,
    ) {
        val expiryTime = days.toExpiryMillis()
        localDataSource.saveMaintenanceIgnoreInfo(maintenanceId, expiryTime)
    }

    override suspend fun markHomeNoticeDialogAsIgnored(
        homeNoticeId: Int,
        days: Int,
    ) {
        val expiryTime = days.toExpiryMillis()
        localDataSource.saveHomeNoticeIgnoreInfo(homeNoticeId, expiryTime)
    }

    override fun isPastCheckInAdEnabled(): Boolean = remoteDataSource.getBoolean("is_past_check_in_ad_enabled")

    private fun Int.toExpiryMillis(): Long = clock.now().toEpochMilliseconds() + (this * 86400_000L)

    companion object {
        private const val DEFAULT_CONFIG_ID = -1
    }
}
