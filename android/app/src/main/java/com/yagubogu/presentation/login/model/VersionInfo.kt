package com.yagubogu.presentation.login.model

data class VersionInfo private constructor(
    val major: Int,
    val minor: Int,
    val patch: Int,
) {
    companion object {
        fun of(versionCode: Int): VersionInfo {
            val major: Int = versionCode / 10000
            val minor: Int = (versionCode % 10000) / 100
            val patch: Int = versionCode % 100
            return VersionInfo(major, minor, patch)
        }
    }
}
