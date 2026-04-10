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

    fun getLastNotificationDate(stadiumId: Int): String? = settings.getStringOrNull("${KEY_LAST_NOTIF_PREFIX}_$stadiumId")

    fun setLastNotificationDate(
        stadiumId: Int,
        date: String,
    ) {
        settings.putString("${KEY_LAST_NOTIF_PREFIX}_$stadiumId", date)
    }

    companion object {
        private const val KEY_GEOFENCE_ENABLED = "geofence_enabled"
        private const val KEY_LAST_NOTIF_PREFIX = "last_notif_date_stadium"
    }
}
