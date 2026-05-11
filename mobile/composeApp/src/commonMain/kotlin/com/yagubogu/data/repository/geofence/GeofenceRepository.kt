package com.yagubogu.data.repository.geofence

import com.kmp.geofence.GeofenceEvent
import kotlinx.coroutines.flow.Flow

interface GeofenceRepository {
    val geofenceEvents: Flow<GeofenceEvent>

    fun isGeofenceEnabled(): Flow<Boolean>

    suspend fun setGeofenceEnabled(enabled: Boolean)

    suspend fun getLastNotificationDate(stadiumId: Int): String?

    suspend fun saveLastNotificationDate(
        stadiumId: Int,
        date: String,
    )

    suspend fun registerAll(): Result<Unit>

    suspend fun unregisterAll(): Result<Unit>
}
