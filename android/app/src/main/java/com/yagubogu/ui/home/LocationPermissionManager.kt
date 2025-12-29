package com.yagubogu.ui.home

import android.Manifest
import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.yagubogu.R
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.util.PermissionUtil

class LocationPermissionManager(
    private val activity: Activity,
) {
    fun isPermissionGranted(): Boolean {
        val isFineLocationPermissionGranted =
            PermissionUtil.isGranted(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        val isCoarseLocationPermissionGranted =
            PermissionUtil.isGranted(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
        return isFineLocationPermissionGranted || isCoarseLocationPermissionGranted
    }

    fun requestPermissions(launcher: ActivityResultLauncher<Array<String>>) {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    fun shouldShowRationale(permission: String) = PermissionUtil.shouldShowRationale(activity, permission)

    fun checkLocationSettingsThenAction(onSuccess: () -> Unit) {
        checkDeviceLocationSettings()
            .addOnSuccessListener {
                // 위치 설정이 활성화된 경우 구장 불러오기
                onSuccess()
            }.addOnFailureListener { exception: Exception ->
                // 다이얼로그 띄워서 사용자가 GPS 켜도록 안내
                if (exception is ResolvableApiException) {
                    exception.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                } else {
                    activity.showToast(R.string.home_location_settings_disabled)
                }
            }
    }

    private fun checkDeviceLocationSettings(): Task<LocationSettingsResponse> {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build()
        val locationSettingsRequest =
            LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

        return LocationServices
            .getSettingsClient(activity)
            .checkLocationSettings(locationSettingsRequest)
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1001
    }
}
