package com.yagubogu.data.repository

import com.yagubogu.data.datasource.LocationLocalDataSource
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.repository.LocationRepository

class LocationDefaultRepository(
    private val locationLocalDataSource: LocationLocalDataSource,
) : LocationRepository {
    override fun getCurrentCoordinate(
        onSuccess: (Coordinate) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        locationLocalDataSource.getCurrentCoordinate(onSuccess, onFailure)
    }

    override fun getDistanceInMeters(
        coordinate: Coordinate,
        targetCoordinate: Coordinate,
    ): Distance = locationLocalDataSource.getDistanceInMeters(coordinate, targetCoordinate)
}
