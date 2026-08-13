package com.yagubogu.data.datasource.appconfig

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppConfigDataStoreLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : AppConfigLocalDataSource {
    override val maintenanceIgnoreInfo: Flow<IgnoreInfo> =
        dataStore.data.map { prefs ->
            IgnoreInfo(
                lastIgnoredId = prefs[MAINTENANCE_LAST_IGNORED_ID_KEY] ?: -1,
                ignoreUntil = prefs[MAINTENANCE_IGNORE_UNTIL_KEY] ?: 0L,
            )
        }

    override val homeNoticeIgnoreInfo: Flow<IgnoreInfo> =
        dataStore.data.map { prefs ->
            IgnoreInfo(
                lastIgnoredId = prefs[HOME_NOTICE_LAST_IGNORED_ID_KEY] ?: -1,
                ignoreUntil = prefs[HOME_NOTICE_IGNORE_UNTIL_KEY] ?: 0L,
            )
        }

    override suspend fun saveMaintenanceIgnoreInfo(
        id: Int,
        expiryTime: Long,
    ) {
        dataStore.edit { prefs ->
            prefs[MAINTENANCE_LAST_IGNORED_ID_KEY] = id
            prefs[MAINTENANCE_IGNORE_UNTIL_KEY] = expiryTime
        }
    }

    override suspend fun saveHomeNoticeIgnoreInfo(
        id: Int,
        expiryTime: Long,
    ) {
        dataStore.edit { prefs ->
            prefs[HOME_NOTICE_LAST_IGNORED_ID_KEY] = id
            prefs[HOME_NOTICE_IGNORE_UNTIL_KEY] = expiryTime
        }
    }

    companion object {
        private val MAINTENANCE_LAST_IGNORED_ID_KEY = intPreferencesKey("maintenance_last_ignored_id")
        private val MAINTENANCE_IGNORE_UNTIL_KEY = longPreferencesKey("maintenance_ignore_until")

        private val HOME_NOTICE_LAST_IGNORED_ID_KEY = intPreferencesKey("home_notice_last_ignored_id")
        private val HOME_NOTICE_IGNORE_UNTIL_KEY = longPreferencesKey("home_notice_ignore_until")
    }
}
