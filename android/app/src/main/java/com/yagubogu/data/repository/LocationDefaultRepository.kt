package com.yagubogu.data.repository

import com.yagubogu.data.datasource.location.LocationDataSource
import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.dto.response.location.DistanceDto
import com.yagubogu.domain.repository.LocationRepository
import javax.inject.Inject

class LocationDefaultRepository @Inject constructor(
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
