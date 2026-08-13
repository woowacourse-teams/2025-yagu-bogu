package com.yagubogu.data.datasource.location

import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.dto.response.location.DistanceDto

interface LocationDataSource {
    fun getCurrentCoordinate(
        onSuccess: (CoordinateDto) -> Unit,
        onFailure: (Exception) -> Unit,
    )

    fun getDistanceInMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
    ): DistanceDto
}
