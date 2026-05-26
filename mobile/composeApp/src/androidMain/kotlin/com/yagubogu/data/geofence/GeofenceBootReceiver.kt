package com.yagubogu.data.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.touchlab.kermit.Logger
import com.kmp.geofence.GeofenceContext
import com.yagubogu.data.repository.geofence.GeofenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBootReceiver :
    BroadcastReceiver(),
    KoinComponent {
    val geofenceRepository: GeofenceRepository by inject()
    private val logger = Logger.withTag("GeofenceBootReceiver")

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isEnabled = geofenceRepository.isGeofenceEnabled().first()

                if (isEnabled) {
                    GeofenceContext.init(context)
                    geofenceRepository.registerAll()
                }
            } catch (e: Exception) {
                logger.e(e) { "지오펜싱 등록 실패" }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
