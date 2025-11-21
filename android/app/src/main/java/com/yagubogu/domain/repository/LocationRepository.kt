package com.yagubogu.domain.repository

import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.dto.response.location.DistanceDto

interface LocationRepository {
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
