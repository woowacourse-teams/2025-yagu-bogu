package com.yagubogu.data.datasource.location

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance

interface LocationDataSource {
    fun getCurrentCoordinate(
        onSuccess: (Coordinate) -> Unit,
        onFailure: (Exception) -> Unit,
    )

    fun getDistanceInMeters(
        coordinate: Coordinate,
        targetCoordinate: Coordinate,
    ): Distance
}
