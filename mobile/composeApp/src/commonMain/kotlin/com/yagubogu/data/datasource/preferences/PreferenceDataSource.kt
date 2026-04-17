package com.yagubogu.data.datasource.preferences

interface PreferenceDataSource {
    fun getString(
        key: String,
        defaultValue: String?,
    ): String?

    fun putString(
        key: String,
        value: String,
    )

    fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean

    fun putBoolean(
        key: String,
        value: Boolean,
    )
}
