package com.yagubogu.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ScoreWidgetDeviceId(
    private val dataStore: DataStore<Preferences>,
) {
    private val mutex = Mutex()

    suspend fun get(): String =
        mutex.withLock {
            dataStore.data.first()[DEVICE_ID_KEY] ?: createAndSave()
        }

    private suspend fun createAndSave(): String {
        val deviceId: String = generateDeviceId()
        dataStore.edit { preferences ->
            preferences[DEVICE_ID_KEY] = deviceId
        }
        return deviceId
    }

    private companion object {
        val DEVICE_ID_KEY = stringPreferencesKey("score_widget_device_id")
    }
}
