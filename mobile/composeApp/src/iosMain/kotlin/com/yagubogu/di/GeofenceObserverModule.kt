package com.yagubogu.di

import com.yagubogu.data.geofence.IosGeofenceObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val GeofenceObserverModule =
    module {
        single(createdAtStart = true) {
            IosGeofenceObserver(
                geofenceRepository = get(),
                sendNotificationUseCase = get(),
                scope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
            ).apply { start() }
        }
    }
