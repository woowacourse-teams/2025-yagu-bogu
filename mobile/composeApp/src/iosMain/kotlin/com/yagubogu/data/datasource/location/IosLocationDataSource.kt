package com.yagubogu.data.datasource.location

import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.dto.response.location.DistanceDto
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosLocationDataSource : LocationDataSource {
    private val locationManager = CLLocationManager()

    // 비동기 콜백을 일회성으로 처리하기 위해 상태를 저장할 변수
    private var onSuccessCallback: ((CoordinateDto) -> Unit)? = null
    private var onFailureCallback: ((Exception) -> Unit)? = null

    // iOS의 Delegate 패턴을 Kotlin 객체로 구현 (메모리 해제 방지를 위해 멤버 변수로 유지)
    private val locationDelegate =
        object : NSObject(), CLLocationManagerDelegateProtocol {
            // 위치를 성공적으로 가져왔을 때 호출됨
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>,
            ) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation
                if (location != null) {
                    // iOS의 CLLocationCoordinate2D 구조체 값은 useContents로 꺼내야 함
                    val coordinateDto =
                        location.coordinate.useContents {
                            CoordinateDto(latitude, longitude)
                        }
                    onSuccessCallback?.invoke(coordinateDto)
                } else {
                    onFailureCallback?.invoke(Exception("위치 정보를 찾을 수 없습니다."))
                }
                clearCallbacks()
            }

            // 위치를 가져오는 데 실패했을 때 호출됨
            override fun locationManager(
                manager: CLLocationManager,
                didFailWithError: NSError,
            ) {
                onFailureCallback?.invoke(Exception(didFailWithError.localizedDescription))
                clearCallbacks()
            }
        }

    init {
        // 매니저에 Delegate 연결 및 정확도 설정
        locationManager.delegate = locationDelegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }

    override fun getCurrentCoordinate(
        onSuccess: (CoordinateDto) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        // 콜백을 저장해두고, 시스템에 1회성 위치 요청(requestLocation)
        onSuccessCallback = onSuccess
        onFailureCallback = onFailure
        locationManager.requestLocation()
    }

    override fun getDistanceInMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
    ): DistanceDto {
        // CLLocation 객체 2개를 만들어 iOS 내장 함수로 거리 계산
        val startLocation = CLLocation(latitude = startLatitude, longitude = startLongitude)
        val endLocation = CLLocation(latitude = endLatitude, longitude = endLongitude)

        // distanceFromLocation은 미터(meter) 단위의 Double(CLLocationDistance)을 반환함
        val distance = startLocation.distanceFromLocation(endLocation)

        return DistanceDto(distance)
    }

    private fun clearCallbacks() {
        onSuccessCallback = null
        onFailureCallback = null
    }
}
