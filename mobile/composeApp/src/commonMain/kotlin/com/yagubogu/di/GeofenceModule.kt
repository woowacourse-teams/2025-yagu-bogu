package com.yagubogu.di

import com.kmp.geofence.createGeofenceManager
import com.yagubogu.domain.geofence.SendGeofenceNotificationUseCase
import org.koin.dsl.module

val geofenceModule =
    module {
        single<SendGeofenceNotificationUseCase> {
            SendGeofenceNotificationUseCase(
                notificationService = get(),
                stadiumRepository = get(),
                geofenceRepository = get(),
                clock = get(),
            )
        }

        single { createGeofenceManager() }
    }
