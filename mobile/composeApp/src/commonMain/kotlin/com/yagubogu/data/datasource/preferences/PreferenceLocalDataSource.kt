package com.yagubogu.data.datasource.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : PreferenceDataSource {
    override fun getString(
        key: String,
        defaultValue: String?,
    ): Flow<String?> =
        dataStore.data.map { prefs ->
            prefs[stringPreferencesKey(key)] ?: defaultValue
        }

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(key)] = value
        }
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey(key)] ?: defaultValue
        }

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(key)] = value
        }
    }
}
