package com.yagubogu.domain.geofence

import co.touchlab.kermit.Logger
import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.domain.model.Stadium.Companion.getStadiumById
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.now
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.notification_geofence_body
import yagubogu.composeapp.generated.resources.notification_geofence_title
import kotlin.time.Clock

class SendGeofenceNotificationUseCase(
    private val alarmeeService: AlarmeeService,
    private val stadiumRepository: StadiumRepository,
    private val clock: Clock,
) {
    private val logger = Logger.withTag("SendGeofenceNotificationUseCase")

    suspend operator fun invoke(stadiumId: Int) {
        val stadium = getStadiumById(stadiumId) ?: return

        val stadiumsWithGames =
            stadiumRepository
                .getStadiumsWithGames(LocalDate.now(clock))
                .map { it.toUiModel() }
                .getOrElse { e ->
                    logger.w(e) { "경기 목록 API 호출 실패(geofence)" }
                    return
                }

        if (stadiumsWithGames.isEmpty()) return

        stadiumsWithGames.findNearestTo(
            coordinate =
                Coordinate(
                    latitude = Latitude(stadium.latitude),
                    longitude = Longitude(stadium.longitude),
                ),
            threshold = Distance(GEOFENCE_NOTIFICATION_THRESHOLD_METERS),
            getDistance = ::calculateDistance,
        ) ?: return

        alarmeeService.local.immediate(
            alarmee =
                Alarmee(
                    uuid = "enter_$stadiumId",
                    notificationTitle = getString(Res.string.notification_geofence_title, stadium.name),
                    notificationBody = getString(Res.string.notification_geofence_body),
                    androidNotificationConfiguration =
                        AndroidNotificationConfiguration(
                            priority = AndroidNotificationPriority.HIGH,
                            channelId = "geofenceChannelId",
                        ),
                    iosNotificationConfiguration = IosNotificationConfiguration(),
                ),
        )
    }

    companion object {
        private const val GEOFENCE_NOTIFICATION_THRESHOLD_METERS = 1000.0
    }
}
