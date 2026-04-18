package com.yagubogu.di

import com.yagubogu.domain.geofence.SendGeofenceNotificationUseCase
import org.koin.dsl.module

val geofenceUseCaseModule =
    module {
        single<SendGeofenceNotificationUseCase> {
            SendGeofenceNotificationUseCase(
                notificationService = get(),
                stadiumRepository = get(),
                geofenceRepository = get(),
                clock = get(),
            )
        }
    }
