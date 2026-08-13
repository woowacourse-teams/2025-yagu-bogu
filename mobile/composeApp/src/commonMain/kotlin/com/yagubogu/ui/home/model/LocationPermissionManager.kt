package com.yagubogu.ui.home.model

interface LocationPermissionManager {
    fun isPermissionGranted(): Boolean

    fun shouldShowRationale(): Boolean

    fun requestPermissions()

    fun checkLocationSettingsThenAction(
        onSuccess: () -> Unit,
        onSettingsDisabled: () -> Unit,
    )
}
