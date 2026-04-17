package com.yagubogu.data.repository.geofence

import com.kmp.geofence.GeofenceEvent
import kotlinx.coroutines.flow.Flow

interface GeofenceRepository {
    val geofenceEvents: Flow<GeofenceEvent>
    var isGeofenceEnabled: Boolean

    fun getLastNotificationDate(stadiumId: Int): String?

    fun saveLastNotificationDate(
        stadiumId: Int,
        date: String,
    )

    suspend fun registerAll(): Result<Unit>

    suspend fun unregisterAll(): Result<Unit>
}
