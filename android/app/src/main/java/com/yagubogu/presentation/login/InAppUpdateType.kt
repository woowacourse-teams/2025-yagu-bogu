package com.yagubogu.presentation.login

enum class InAppUpdateType {
    IMMEDIATE, // 업데이트 강제
    FLEXIBLE, // 업데이트 권장
    NONE,
    ;

    companion object {
        fun determine(
            currentVersionInfo: VersionInfo,
            availableVersionInfo: VersionInfo,
        ): InAppUpdateType =
            when {
                availableVersionInfo.major > currentVersionInfo.major -> IMMEDIATE
                availableVersionInfo.minor > currentVersionInfo.minor -> FLEXIBLE
                else -> NONE
            }
    }
}
