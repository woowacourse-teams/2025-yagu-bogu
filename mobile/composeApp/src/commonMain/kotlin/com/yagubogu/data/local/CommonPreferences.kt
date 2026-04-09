package com.yagubogu.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class CommonPreferences {
    private val settings: Settings = Settings()

    var geofenceEnabled: Boolean
        get() = settings.getBoolean(KEY_GEOFENCE_ENABLED, defaultValue = false)
        set(value) {
            settings[KEY_GEOFENCE_ENABLED] = value
        }

    companion object {
        private const val KEY_GEOFENCE_ENABLED = "geofence_enabled"
    }
}
