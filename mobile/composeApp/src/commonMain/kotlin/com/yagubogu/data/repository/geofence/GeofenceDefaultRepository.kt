package com.yagubogu.data.repository.geofence

import com.yagubogu.data.datasource.preferences.LocalPreferenceDataSource

class GeofenceDefaultRepository(
    private val dataSource: LocalPreferenceDataSource,
) : GeofenceRepository {
    override var isGeofenceEnabled: Boolean
        get() = dataSource.getBoolean(KEY_GEOFENCE_ENABLED, false)
        set(value) = dataSource.putBoolean(KEY_GEOFENCE_ENABLED, value)

    override fun getLastNotificationDate(stadiumId: Int): String? = dataSource.getString("${KEY_LAST_NOTIF_PREFIX}_$stadiumId", null)

    override fun saveLastNotificationDate(
        stadiumId: Int,
        date: String,
    ) = dataSource.putString("${KEY_LAST_NOTIF_PREFIX}_$stadiumId", date)

    companion object {
        private const val KEY_GEOFENCE_ENABLED = "geofence_enabled"
        private const val KEY_LAST_NOTIF_PREFIX = "last_notif_date_stadium"
    }
}
