package com.yagubogu.data.datasource.geofence

import com.kmp.geofence.GeofenceEvent
import com.yagubogu.domain.model.Stadium
import kotlinx.coroutines.flow.Flow

interface GeofenceDataSource {
    val geofenceEvents: Flow<GeofenceEvent>

    suspend fun addAllGeofences(stadiums: List<Stadium>): Result<Unit>

    suspend fun removeAllGeofences(ids: List<String>): Result<Unit>
}
