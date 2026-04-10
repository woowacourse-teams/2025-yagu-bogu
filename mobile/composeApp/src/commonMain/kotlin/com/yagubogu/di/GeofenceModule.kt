package com.yagubogu.di

import com.yagubogu.domain.geofence.SendGeofenceNotificationUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val geofenceModule: Module

val geofenceUseCaseModule =
    module {
        single<SendGeofenceNotificationUseCase> {
            SendGeofenceNotificationUseCase(
                alarmeeService = get(),
                stadiumRepository = get(),
                clock = get(),
            )
        }
    }
