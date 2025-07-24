package com.yagubogu.data.repository

import com.yagubogu.data.datasource.LocationDataSource
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.repository.LocationRepository

class LocationDefaultRepository(
    private val locationDataSource: LocationDataSource,
) : LocationRepository {
    override fun getCurrentCoordinate(
        onSuccess: (Coordinate) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        locationDataSource.getCurrentCoordinate(onSuccess, onFailure)
    }

    override fun getDistanceInMeters(
        coordinate: Coordinate,
        targetCoordinate: Coordinate,
    ): Distance = locationDataSource.getDistanceInMeters(coordinate, targetCoordinate)
}
