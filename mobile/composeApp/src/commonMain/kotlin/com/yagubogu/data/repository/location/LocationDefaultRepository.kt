package com.yagubogu.data.repository.location

import com.yagubogu.data.datasource.location.LocationDataSource
import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.dto.response.location.DistanceDto

class LocationDefaultRepository(
    private val locationDataSource: LocationDataSource,
) : LocationRepository {
    override fun getCurrentCoordinate(
        onSuccess: (CoordinateDto) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        locationDataSource.getCurrentCoordinate(onSuccess, onFailure)
    }

    override fun getDistanceInMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
    ): DistanceDto =
        locationDataSource.getDistanceInMeters(
            startLatitude,
            startLongitude,
            endLatitude,
            endLongitude,
        )
}
