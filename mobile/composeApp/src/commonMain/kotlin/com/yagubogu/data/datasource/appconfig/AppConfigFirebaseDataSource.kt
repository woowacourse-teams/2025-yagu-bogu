package com.yagubogu.data.datasource.appconfig

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig
import kotlin.time.Duration.Companion.minutes

class AppConfigFirebaseDataSource(
    private val remoteConfig: FirebaseRemoteConfig,
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
}
