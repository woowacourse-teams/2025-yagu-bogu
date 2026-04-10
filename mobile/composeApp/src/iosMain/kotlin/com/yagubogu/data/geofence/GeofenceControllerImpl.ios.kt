package com.yagubogu.data.geofence

import co.touchlab.kermit.Logger
import com.kmp.geofence.GeofenceEvent
import com.kmp.geofence.GeofenceEventListener
import com.kmp.geofence.createGeofenceManager
import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import com.yagubogu.domain.geofence.GeofenceController
import com.yagubogu.domain.model.Stadium
import com.yagubogu.domain.model.Stadium.Companion.getStadiumById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.getString
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.notification_geofence_body
import yagubogu.composeapp.generated.resources.notification_geofence_title
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeofenceControllerImpl(
    private val alarmeeService: AlarmeeService,
) : GeofenceController {
    private val manager = createGeofenceManager()
    private val logger = Logger.withTag("GeofenceControllerImplIos")

    override suspend fun registerAll(): Result<Unit> =
        runCatching {
            manager.setGeofenceEventListener(
                object : GeofenceEventListener {
                    override fun onGeofenceEnter(event: GeofenceEvent) {
                        val stadiumId = event.geofenceId.toIntOrNull() ?: return
                        val stadium = getStadiumById(stadiumId) ?: return
                        CoroutineScope(Dispatchers.Default).launch {
                            logger.i { "ios 지오펜스 입장: $stadiumId" }
                            val notificationTitle = getString(Res.string.notification_geofence_title, stadium.name)
                            val notificationBody = getString(Res.string.notification_geofence_body)

                            alarmeeService.local.immediate(
                                alarmee =
                                    Alarmee(
                                        uuid = "enter_$stadiumId",
                                        notificationTitle = notificationTitle,
                                        notificationBody = notificationBody,
                                        androidNotificationConfiguration =
                                            AndroidNotificationConfiguration(
                                                priority = AndroidNotificationPriority.HIGH,
                                                channelId = "geofenceChannelId",
                                            ),
                                        iosNotificationConfiguration = IosNotificationConfiguration(),
                                    ),
                            )
                        }
                    }

                    override fun onGeofenceExit(event: GeofenceEvent) = Unit
                },
            )

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
