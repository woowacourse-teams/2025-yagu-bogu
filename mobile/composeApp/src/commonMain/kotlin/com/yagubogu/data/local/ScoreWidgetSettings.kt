package com.yagubogu.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScoreWidgetSettings(
    private val dataStore: DataStore<Preferences>,
) {
    val isEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[ENABLED_KEY] ?: false
        }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLED_KEY] = enabled
        }
    }

    private companion object {
        val ENABLED_KEY = booleanPreferencesKey("score_widget_enabled")
    }
}
