package com.yagubogu.data.datasource.appconfig

data class IgnoreInfo(
    val lastIgnoredId: Int,
    val ignoreUntil: Long,
) {
    fun shouldShow(
        id: Int,
        currentTimeMillis: Long,
    ): Boolean = id > lastIgnoredId || currentTimeMillis > ignoreUntil
}
