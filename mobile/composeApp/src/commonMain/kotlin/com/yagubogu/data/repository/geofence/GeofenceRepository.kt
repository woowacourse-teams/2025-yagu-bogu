package com.yagubogu.data.repository.geofence

interface GeofenceRepository {
    var isGeofenceEnabled: Boolean

    fun getLastNotificationDate(stadiumId: Int): String?

    fun saveLastNotificationDate(
        stadiumId: Int,
        date: String,
    )
}
