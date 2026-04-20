package com.yagubogu.data.geofence

import com.yagubogu.data.repository.geofence.GeofenceRepository
import com.yagubogu.domain.geofence.SendGeofenceNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class IosGeofenceObserver(
    private val geofenceRepository: GeofenceRepository,
    private val sendNotificationUseCase: SendGeofenceNotificationUseCase,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun start() {
        if (job != null) return

        job = scope.launch {
            geofenceRepository.geofenceEvents.collect { event ->
                val stadiumId = event.geofenceId.toIntOrNull() ?: return@collect
                sendNotificationUseCase(stadiumId)
            }
        }
    }
}
