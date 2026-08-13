package com.yagubogu.data.datasource.appconfig

import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.appconfig.HomeNoticeResponse
import com.yagubogu.data.dto.response.appconfig.MaintenanceResponse
import dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

class AppConfigFirebaseDataSource(
    private val remoteConfig: FirebaseRemoteConfig,
    private val json: Json,
) : AppConfigRemoteDataSource {
    private var isConfigured = false

    private suspend fun ensureConfigured() {
        if (isConfigured) return
        remoteConfig.settings {
            minimumFetchInterval = 15.minutes
        }

        remoteConfig.setDefaults(
            "is_maintenance" to false,
            "maintenance_message" to "",
            "maintenance" to json.encodeToString(MaintenanceResponse()),
            "home_notice" to json.encodeToString(HomeNoticeResponse()),
        )
        isConfigured = true
    }

    override suspend fun fetchAndActivate() {
        ensureConfigured()
        try {
            remoteConfig.fetchAndActivate()
            Logger.d { "Firebase Remote Config fetch 성공" }
        } catch (e: Exception) {
            Logger.e(e) { "Firebase Remote Config fetch 실패 및 기본값 사용" }
        }
    }

    override fun getBoolean(key: String): Boolean = remoteConfig.getValue(key).asBoolean()

    override fun getString(key: String): String = remoteConfig.getValue(key).asString()

    override fun getMaintenanceResponse(): MaintenanceResponse {
        val jsonString = remoteConfig.getValue("maintenance").asString()
        return try {
            json.decodeFromString<MaintenanceResponse>(jsonString)
        } catch (e: Exception) {
            MaintenanceResponse()
        }
    }

    override fun getHomeNoticeResponse(): HomeNoticeResponse {
        val jsonString = remoteConfig.getValue("home_notice").asString()
        return try {
            json.decodeFromString<HomeNoticeResponse>(jsonString)
        } catch (e: Exception) {
            HomeNoticeResponse()
        }
    }
}
