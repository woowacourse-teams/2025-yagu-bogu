package com.yagubogu.data.geofence

import co.touchlab.kermit.Logger
import com.kmp.geofence.GeofenceEvent
import com.kmp.geofence.GeofenceEventListener
import com.kmp.geofence.createGeofenceManager
import com.yagubogu.domain.geofence.GeofenceController
import com.yagubogu.domain.geofence.SendGeofenceNotificationUseCase
import com.yagubogu.domain.model.Stadium
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeofenceControllerImpl(
    private val sendGeofenceNotificationUseCase: SendGeofenceNotificationUseCase,
) : GeofenceController {
    private val manager = createGeofenceManager()
    private val logger = Logger.withTag("GeofenceControllerImpl")

    init {
        manager.checkLocationPermissions()
        manager.setGeofenceEventListener(
            object : GeofenceEventListener {
                override fun onGeofenceEnter(event: GeofenceEvent) {
                    val stadiumId = event.geofenceId.toIntOrNull() ?: return
                    CoroutineScope(Dispatchers.Default).launch {
                        logger.i { "지오펜스 이벤트 수신: $stadiumId" }
                        sendGeofenceNotificationUseCase(stadiumId)
                    }
                }

                override fun onGeofenceExit(event: GeofenceEvent) = Unit
            },
        )
    }

    override suspend fun registerAll(): Result<Unit> =
        runCatching {
            Stadium.ALL_LIST.forEach { stadium ->
                suspendCancellableCoroutine { cont ->
                    manager.addGeofence(
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

    override suspend fun unregisterAll(): Result<Unit> =
        runCatching {
            suspendCancellableCoroutine { cont ->
                manager.removeGeofences(
                    ids = Stadium.ALL_LIST.map { it.id.toString() },
                    onSuccess = {
                        logger.i { "지오펜스 등록 해제 성공: ${Stadium.ALL_LIST.map { it.id }}" }
                        cont.resume(Unit)
                    },
                    onFailure = { error -> cont.resumeWithException(Exception(error)) },
                )
            }
        }
}
