package com.yagubogu.data.repository.geofence

import com.kmp.geofence.GeofenceEvent
import com.yagubogu.data.datasource.geofence.GeofenceDataSource
import com.yagubogu.data.datasource.preferences.PreferenceDataSource
import com.yagubogu.domain.model.Stadium
import kotlinx.coroutines.flow.Flow

class GeofenceDefaultRepository(
    private val preferenceDataSource: PreferenceDataSource,
    private val geofenceDataSource: GeofenceDataSource,
) : GeofenceRepository {
    override val geofenceEvents: Flow<GeofenceEvent> = geofenceDataSource.geofenceEvents

    override var isGeofenceEnabled: Boolean
        get() = preferenceDataSource.getBoolean(KEY_GEOFENCE_ENABLED, false)
        set(value) = preferenceDataSource.putBoolean(KEY_GEOFENCE_ENABLED, value)

    override fun getLastNotificationDate(stadiumId: Int): String? =
        preferenceDataSource.getString("${KEY_LAST_NOTIF_PREFIX}_$stadiumId", null)

    override fun saveLastNotificationDate(
        stadiumId: Int,
        date: String,
    ) = preferenceDataSource.putString("${KEY_LAST_NOTIF_PREFIX}_$stadiumId", date)

    override suspend fun registerAll(): Result<Unit> = geofenceDataSource.addAllGeofences(Stadium.ALL_LIST)

    override suspend fun unregisterAll(): Result<Unit> = geofenceDataSource.removeAllGeofences(Stadium.ALL_LIST.map { it.id.toString() })

    companion object {
        private const val KEY_GEOFENCE_ENABLED = "geofence_enabled"
        private const val KEY_LAST_NOTIF_PREFIX = "last_notif_date_stadium"
    }
}
