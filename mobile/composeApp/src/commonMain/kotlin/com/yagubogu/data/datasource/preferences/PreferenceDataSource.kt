package com.yagubogu.data.datasource.preferences

import kotlinx.coroutines.flow.Flow

interface PreferenceDataSource {
    fun getString(
        key: String,
        defaultValue: String?,
    ): Flow<String?>

    suspend fun putString(
        key: String,
        value: String,
    )

    fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean>

    suspend fun putBoolean(
        key: String,
        value: Boolean,
    )
}
