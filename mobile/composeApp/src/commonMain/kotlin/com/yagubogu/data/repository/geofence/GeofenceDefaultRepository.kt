package com.yagubogu.data.repository.geofence

import com.kmp.geofence.GeofenceEvent
import com.yagubogu.data.datasource.geofence.GeofenceDataSource
import com.yagubogu.data.datasource.preferences.PreferenceDataSource
import com.yagubogu.domain.model.Stadium
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GeofenceDefaultRepository(
    private val preferenceDataSource: PreferenceDataSource,
    private val geofenceDataSource: GeofenceDataSource,
) : GeofenceRepository {
    override val iosGeofenceEvents: Flow<GeofenceEvent> = geofenceDataSource.iosGeofenceEvents

    override fun isGeofenceEnabled(): Flow<Boolean> = preferenceDataSource.getBoolean(KEY_GEOFENCE_ENABLED, false)

    override suspend fun setGeofenceEnabled(enabled: Boolean) {
        preferenceDataSource.putBoolean(KEY_GEOFENCE_ENABLED, enabled)
    }

    override suspend fun getLastNotificationDate(stadiumId: Int): String? =
        preferenceDataSource.getString("${KEY_LAST_NOTIF_PREFIX}_$stadiumId", null).first()

    override suspend fun saveLastNotificationDate(
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
