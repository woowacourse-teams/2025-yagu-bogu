package com.yagubogu.data.geofence

import co.touchlab.kermit.Logger
import com.kmp.geofence.createGeofenceManager
import com.yagubogu.domain.geofence.GeofenceController
import com.yagubogu.domain.model.Stadium
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeofenceControllerImpl : GeofenceController {
    private val manager = createGeofenceManager()
    private val logger = Logger.withTag("GeofenceControllerImplAndroid")

    override suspend fun registerAll(): Result<Unit> =
        runCatching {
            Stadium.ALL_LIST.forEach { stadium: Stadium ->
                suspendCancellableCoroutine { cont ->
                    manager.addGeofence(
                        id = stadium.id.toString(),
                        latitude = stadium.latitude,
                        longitude = stadium.longitude,
                        radius = Stadium.GEOFENCE_RADIUS_METERS,
                        onSuccess = {
                            logger.d { "지오펜스 등록 성공: ${stadium.id}" }
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
                        logger.d { "지오펜스 등록 해제 성공: ${Stadium.ALL_LIST.map { it.id }}" }
                        cont.resume(Unit)
                    },
                    onFailure = { error -> cont.resumeWithException(Exception(error)) },
                )
            }
        }
}
