package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.location.CoordinateDto
import com.yagubogu.data.dto.response.location.DistanceDto
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude

fun CoordinateDto.toDomain(): Coordinate {
    val latitude = Latitude(latitude)
    val longitude = Longitude(longitude)
    return Coordinate(latitude, longitude)
}

fun DistanceDto.toDomain(): Distance = Distance(distance)
