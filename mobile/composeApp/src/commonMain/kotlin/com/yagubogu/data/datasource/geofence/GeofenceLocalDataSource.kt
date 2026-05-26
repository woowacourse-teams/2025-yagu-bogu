package com.yagubogu.data.datasource.geofence

import co.touchlab.kermit.Logger
import com.kmp.geofence.GeofenceEvent
import com.kmp.geofence.GeofenceEventListener
import com.kmp.geofence.GeofenceManager
import com.yagubogu.domain.model.Stadium
import com.yagubogu.ui.common.platform.PlatformType
import com.yagubogu.ui.common.platform.currentPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeofenceLocalDataSource(
    private val geofenceManager: GeofenceManager,
) : GeofenceDataSource {
    private val logger = Logger.withTag("GeofenceLocalDataSource")

    private val _iosGeofenceEvents = MutableSharedFlow<GeofenceEvent>(replay = 1, extraBufferCapacity = 64)
    override val iosGeofenceEvents: Flow<GeofenceEvent> =
        _iosGeofenceEvents.asSharedFlow()

    init {
        // GeofenceManager 내부 CLLocationManager가 lazy로 선언되어 있어
        // 앱 재실행 시 didDetermineState 콜백 전에 delegate가 연결되도록
        // 강제로 lazy 초기화를 트리거하는 우회 코드
        geofenceManager.checkLocationPermissions()
        initIosGeofenceEventListener()
    }

    override suspend fun addAllGeofences(stadiums: List<Stadium>): Result<Unit> =
        runCatching {
            stadiums.forEach { stadium ->
                suspendCancellableCoroutine { cont ->
                    geofenceManager.addGeofence(
                        id = stadium.id.toString(),
                        latitude = stadium.latitude,
                        longitude = stadium.longitude,
                        radius = Stadium.GEOFENCE_RADIUS_METERS,
                        onSuccess = {
                            logger.i { "지오펜스 등록 성공: ${stadium.id}" }
                            cont.resume(Unit)
                        },
                        onFailure = { error -> cont.resumeWithException(Exception(error)) },
                    )
                }
            }
        }

    override suspend fun removeAllGeofences(ids: List<String>): Result<Unit> =
        runCatching {
            suspendCancellableCoroutine { cont ->
                geofenceManager.removeGeofences(
                    ids = Stadium.ALL_LIST.map { it.id.toString() },
                    onSuccess = {
                        logger.i { "지오펜스 등록 해제 성공: ${Stadium.ALL_LIST.map { it.id }}" }
                        cont.resume(Unit)
                    },
                    onFailure = { error -> cont.resumeWithException(Exception(error)) },
                )
            }
        }

    private fun initIosGeofenceEventListener() {
        if (currentPlatform == PlatformType.IOS) {
            geofenceManager.setGeofenceEventListener(
                object : GeofenceEventListener {
                    override fun onGeofenceEnter(event: GeofenceEvent) {
                        logger.i { "지오펜스 입장 감지: ${event.geofenceId}" }
                        _iosGeofenceEvents.tryEmit(event)
                    }

                    override fun onGeofenceExit(event: GeofenceEvent) {
                        _iosGeofenceEvents.tryEmit(event)
                    }
                },
            )
        }
    }
}
