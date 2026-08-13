package com.yagubogu.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.yagubogu.ui.home.model.LocationPermissionManager
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject

// 권한 상태 변경을 수신하기 위한 델리게이트
private class LocationDelegate(
    private val onPermissionResult: (Map<String, Boolean>) -> Unit,
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    private var initialized = false

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        // CLLocationManager에 delegate 할당 시 즉시 발생하는 초기 콜백 무시
        if (!initialized) {
            initialized = true
            return
        }

        val isGranted =
            manager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
                manager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways

        // NotDetermined 상태에서는 아직 선택 전이므로 콜백을 무시
        if (manager.authorizationStatus != kCLAuthorizationStatusNotDetermined) {
            onPermissionResult(mapOf("ios_location" to isGranted))
        }
    }
}

@Composable
actual fun rememberLocationPermissionManager(onPermissionResult: (Map<String, Boolean>) -> Unit): LocationPermissionManager {
    val delegate = remember { LocationDelegate(onPermissionResult) }

    return remember {
        object : LocationPermissionManager {
            private val locationManager =
                CLLocationManager().apply {
                    this.delegate = delegate
                }

            override fun isPermissionGranted(): Boolean {
                val status = locationManager.authorizationStatus
                return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    status == kCLAuthorizationStatusAuthorizedAlways
            }

            override fun shouldShowRationale(): Boolean {
                // iOS는 권한 거절 시 즉시 설정 화면으로 유도하는 것이 일반적
                return false
            }

            override fun requestPermissions() {
                val status = locationManager.authorizationStatus
                if (status == kCLAuthorizationStatusNotDetermined) {
                    locationManager.requestWhenInUseAuthorization()
                    // 결과는 Delegate의 locationManagerDidChangeAuthorization에서 처리됨
                } else {
                    onPermissionResult(mapOf("ios_location" to isPermissionGranted()))
                }
            }

            override fun checkLocationSettingsThenAction(
                onSuccess: () -> Unit,
                onSettingsDisabled: () -> Unit,
            ) {
                // iOS의 시스템 위치 서비스 켜짐 여부 확인
                if (CLLocationManager.locationServicesEnabled()) {
                    onSuccess()
                } else {
                    onSettingsDisabled()
                }
            }
        }
    }
}

@Composable
actual fun rememberAppSettingsOpener(): () -> Unit =
    remember {
        {
            val url = NSURL(string = UIApplicationOpenSettingsURLString)
            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(
                    url = url,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = null,
                )
            }
        }
    }
