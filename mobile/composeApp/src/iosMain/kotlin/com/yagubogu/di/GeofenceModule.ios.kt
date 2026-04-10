package com.yagubogu.di

import com.yagubogu.data.geofence.GeofenceControllerImpl
import com.yagubogu.domain.geofence.GeofenceController
import org.koin.dsl.module

actual val geofenceModule =
    module {
        single<GeofenceController> {
            GeofenceControllerImpl(
                sendGeofenceNotificationUseCase = get(),
            )
        }
    }
