package com.yagubogu.domain.repository

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance

interface LocationRepository {
    fun getCurrentCoordinate(
        onSuccess: (Coordinate) -> Unit,
        onFailure: (Exception) -> Unit,
    )

    fun getDistanceInMeters(
        coordinate: Coordinate,
        targetCoordinate: Coordinate,
    ): Distance
}
