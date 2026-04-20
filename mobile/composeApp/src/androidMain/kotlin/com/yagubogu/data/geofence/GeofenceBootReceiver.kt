package com.yagubogu.data.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kmp.geofence.GeofenceContext
import com.yagubogu.data.repository.geofence.GeofenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBootReceiver :
    BroadcastReceiver(),
    KoinComponent {
    val geofenceRepository: GeofenceRepository by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        if (!geofenceRepository.isGeofenceEnabled) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                GeofenceContext.init(context)
                geofenceRepository.registerAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
