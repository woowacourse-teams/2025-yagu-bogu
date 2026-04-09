package com.yagubogu.domain.geofence

interface GeofenceController {
    suspend fun registerAll(): Result<Unit>

    suspend fun unregisterAll(): Result<Unit>
}
