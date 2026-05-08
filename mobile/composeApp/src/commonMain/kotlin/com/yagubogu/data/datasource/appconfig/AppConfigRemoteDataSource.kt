package com.yagubogu.data.datasource.appconfig

interface AppConfigRemoteDataSource {
    suspend fun fetchAndActivate()

    fun getBoolean(key: String): Boolean

    fun getString(key: String): String
}
