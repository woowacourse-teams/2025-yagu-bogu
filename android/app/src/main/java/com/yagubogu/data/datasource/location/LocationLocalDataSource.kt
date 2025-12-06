package com.yagubogu.data.datasource.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.dto.response.location.DistanceDto
import javax.inject.Inject

class LocationLocalDataSource @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
) : LocationDataSource {
    @SuppressLint("MissingPermission")
    override fun getCurrentCoordinate(
        onSuccess: (CoordinateDto) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        locationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location ->
                val coordinateDto = CoordinateDto(location.latitude, location.longitude)
                onSuccess(coordinateDto)
            }.addOnFailureListener { exception: Exception ->
                onFailure(exception)
            }
    }

    override fun getDistanceInMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
    ): DistanceDto {
        val results = FloatArray(RESULTS_ARRAY_SIZE)
        Location.distanceBetween(
            startLatitude,
            startLongitude,
            endLatitude,
            endLongitude,
            results,
        )
        return DistanceDto(results.first().toDouble())
    }

    companion object {
        private const val RESULTS_ARRAY_SIZE = 1
    }
}
