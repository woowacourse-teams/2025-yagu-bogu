package com.yagubogu.data.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TokenManager(
    private val dataStore: DataStore<Preferences>,
) {
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    private val mutex = Mutex()

    /** access token 변경을 관찰한다. 로그인/로그아웃/토큰 갱신 시 emit된다. */
    val accessTokenFlow: Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }

    suspend fun getAccessToken(): String? =
        mutex.withLock {
            if (cachedAccessToken == null) {
                cachedAccessToken = dataStore.data.first()[ACCESS_TOKEN_KEY]
            }
            cachedAccessToken
        }

    suspend fun getRefreshToken(): String? =
        mutex.withLock {
            if (cachedRefreshToken == null) {
                cachedRefreshToken = dataStore.data.first()[REFRESH_TOKEN_KEY]
            }
            cachedRefreshToken
        }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        mutex.withLock {
            cachedAccessToken = accessToken
            cachedRefreshToken = refreshToken
        }

        dataStore.edit { prefs: MutablePreferences ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun clearTokens() {
        mutex.withLock {
            cachedAccessToken = null
            cachedRefreshToken = null
        }

        dataStore.edit { prefs: MutablePreferences ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }
}
