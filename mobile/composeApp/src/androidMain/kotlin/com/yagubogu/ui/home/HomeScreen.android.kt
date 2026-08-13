package com.yagubogu.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.yagubogu.ui.home.model.LocationPermissionManager

@Composable
actual fun rememberLocationPermissionManager(onPermissionResult: (Map<String, Boolean>) -> Unit): LocationPermissionManager {
    val context: Context = LocalContext.current
    val activity: Activity? = LocalActivity.current

    // 권한 요청 런처
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = onPermissionResult,
        )

    // GPS 설정 다이얼로그(ResolvableApiException) 결과를 처리하기 위한 상태 및 런처
    var pendingSuccess by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingDisabled by remember { mutableStateOf<(() -> Unit)?>(null) }

    val settingResolutionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                pendingSuccess?.invoke()
            } else {
                pendingDisabled?.invoke()
            }
            // 실행 후 콜백 초기화
            pendingSuccess = null
            pendingDisabled = null
        }

    return remember(context, activity) {
        object : LocationPermissionManager {
            override fun isPermissionGranted(): Boolean {
                val isFineGranted: Boolean =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                val isCoarseGranted: Boolean =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED

                return isFineGranted || isCoarseGranted
            }

            override fun shouldShowRationale(): Boolean {
                if (activity == null) return false
                return ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
            }

            override fun requestPermissions() {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }

            override fun checkLocationSettingsThenAction(
                onSuccess: () -> Unit,
                onSettingsDisabled: () -> Unit,
            ) {
                if (activity == null) {
                    onSettingsDisabled()
                    return
                }

                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build()
                val request =
                    LocationSettingsRequest
                        .Builder()
                        .addLocationRequest(locationRequest)
                        .setAlwaysShow(true)
                        .build()

                LocationServices
                    .getSettingsClient(activity)
                    .checkLocationSettings(request)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception: Exception ->
                        if (exception is ResolvableApiException) {
                            // 사용자가 GPS를 켜도록 유도하는 시스템 다이얼로그 띄우기
                            pendingSuccess = onSuccess
                            pendingDisabled = onSettingsDisabled
                            try {
                                val intentSenderRequest =
                                    IntentSenderRequest.Builder(exception.resolution).build()
                                settingResolutionLauncher.launch(intentSenderRequest)
                            } catch (e: Exception) {
                                onSettingsDisabled()
                            }
                        } else {
                            onSettingsDisabled()
                        }
                    }
            }
        }
    }
}

@Composable
actual fun rememberAppSettingsOpener(): () -> Unit {
    val context: Context = LocalContext.current
    return remember {
        {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
        }
    }
}
