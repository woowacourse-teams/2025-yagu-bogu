package com.yagubogu.domain.geofence

import co.touchlab.kermit.Logger
import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import com.yagubogu.data.repository.geofence.GeofenceRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.domain.model.Stadium
import com.yagubogu.domain.model.Stadium.Companion.getStadiumById
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.now
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.notification_geofence_body
import yagubogu.composeapp.generated.resources.notification_geofence_title
import kotlin.time.Clock

class SendGeofenceNotificationUseCase(
    private val alarmeeService: AlarmeeService,
    private val stadiumRepository: StadiumRepository,
    private val geofenceRepository: GeofenceRepository,
    private val clock: Clock,
) {
    private val logger = Logger.withTag("SendGeofenceNotificationUseCase")

    private val mutex = Mutex()

    suspend operator fun invoke(stadiumId: Int) {
        mutex.withLock {
            val today = LocalDate.now(clock).toString()
            val lastDate = geofenceRepository.getLastNotificationDate(stadiumId)
            if (lastDate == today) {
                logger.d { "오늘 이미 알림을 보낸 경기장입니다: $stadiumId" }
                return
            }

            val geofenceTriggeredStadium: Stadium = getStadiumById(stadiumId) ?: return

            val stadiumsWithGames =
                stadiumRepository
                    .getStadiumsWithGames(LocalDate.now(clock))
                    .map { it.toUiModel() }
                    .getOrElse { e ->
                        logger.w(e) { "경기 목록 API 호출 실패(geofence)" }
                        return
                    }

            if (stadiumsWithGames.isEmpty()) return

            val todayGameStadium =
                stadiumsWithGames.findNearestTo(
                    coordinate =
                        Coordinate(
                            latitude = Latitude(geofenceTriggeredStadium.latitude),
                            longitude = Longitude(geofenceTriggeredStadium.longitude),
                        ),
                    threshold = Distance(GEOFENCE_NOTIFICATION_THRESHOLD_METERS),
                    getDistance = ::calculateDistance,
                ) ?: return

            logger.i { "지오펜스된 야구장과 가장 가까운 오늘 경기 하는 야구장 사이의 거리: ${todayGameStadium.second} " }
            if (todayGameStadium.second.value > SAME_STADIUM_CHECK_THRESHOLD_METERS) return

            alarmeeService.local.immediate(
                alarmee =
                    Alarmee(
                        uuid = "enter_$stadiumId",
                        notificationTitle =
                            getString(
                                Res.string.notification_geofence_title,
                                geofenceTriggeredStadium.name,
                            ),
                        notificationBody = getString(Res.string.notification_geofence_body),
                        androidNotificationConfiguration =
                            AndroidNotificationConfiguration(
                                priority = AndroidNotificationPriority.HIGH,
                                channelId = "geofenceChannelId",
                            ),
                        iosNotificationConfiguration = IosNotificationConfiguration(),
                    ),
            )

            geofenceRepository.saveLastNotificationDate(stadiumId, today)
            logger.i { "지오펜스 알림 전송 성공: $stadiumId" }
        }
    }

    companion object {
        private const val GEOFENCE_NOTIFICATION_THRESHOLD_METERS = 1000.0
        private const val SAME_STADIUM_CHECK_THRESHOLD_METERS = 5.0
    }
}
