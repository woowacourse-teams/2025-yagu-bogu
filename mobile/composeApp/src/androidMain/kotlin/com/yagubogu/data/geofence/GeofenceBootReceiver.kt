package com.yagubogu.data.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kmp.geofence.GeofenceContext
import com.yagubogu.data.local.CommonPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = CommonPreferences()
        if (!prefs.geofenceEnabled) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                GeofenceContext.init(context)
                GeofenceControllerImpl().registerAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
