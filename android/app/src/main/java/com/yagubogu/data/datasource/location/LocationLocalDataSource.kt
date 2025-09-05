package com.yagubogu.data.datasource.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude

class LocationLocalDataSource(
    private val locationClient: FusedLocationProviderClient,
) : LocationDataSource {
    @SuppressLint("MissingPermission")
    override fun getCurrentCoordinate(
        onSuccess: (Coordinate) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        locationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location ->
                val currentLatitude = Latitude(location.latitude)
                val currentLongitude = Longitude(location.longitude)
                val currentCoordinate = Coordinate(currentLatitude, currentLongitude)
                onSuccess(currentCoordinate)
            }.addOnFailureListener { exception: Exception ->
                onFailure(exception)
            }
    }

    override fun getDistanceInMeters(
        coordinate: Coordinate,
        targetCoordinate: Coordinate,
    ): Distance {
        val results = FloatArray(RESULTS_ARRAY_SIZE)
        Location.distanceBetween(
            coordinate.latitude.value,
            coordinate.longitude.value,
            targetCoordinate.latitude.value,
            targetCoordinate.longitude.value,
            results,
        )
        return Distance(results.first().toDouble())
    }

    companion object {
        private const val RESULTS_ARRAY_SIZE = 1
    }
}
