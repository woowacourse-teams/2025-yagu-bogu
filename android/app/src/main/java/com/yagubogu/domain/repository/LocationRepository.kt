package com.yagubogu.domain.repository

import com.yagubogu.domain.model.Coordinate

interface LocationRepository {
    fun getCurrentCoordinate(
        onSuccess: (Coordinate) -> Unit,
        onFailure: (Exception) -> Unit,
    )

    fun getDistanceInMeters(
        coordinate: Coordinate,
        targetCoordinate: Coordinate,
    ): Float
}
