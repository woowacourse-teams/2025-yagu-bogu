package com.yagubogu.data.datasource.preferences

import com.russhwolf.settings.Settings

class PreferenceDataSource(
    private val settings: Settings,
) : LocalPreferenceDataSource {
    override fun getString(
        key: String,
        defaultValue: String?,
    ) = settings.getStringOrNull(key) ?: defaultValue

    override fun putString(
        key: String,
        value: String,
    ) {
        settings.putString(key, value)
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ) = settings.getBoolean(key, defaultValue)

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        settings.putBoolean(key, value)
    }
}
