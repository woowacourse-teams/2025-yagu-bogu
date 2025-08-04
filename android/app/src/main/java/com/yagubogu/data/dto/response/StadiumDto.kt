package com.yagubogu.data.dto.response

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.domain.model.Stadium
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumDto(
    @SerialName("id")
    val id: Long, // 구장 id
    @SerialName("fullName")
    val fullName: String, // 구장 이름
    @SerialName("shortName")
    val shortName: String, // 구장 별명
    @SerialName("location")
    val location: String, // 위치
    @SerialName("latitude")
    val latitude: Double, // 위도
    @SerialName("longitude")
    val longitude: Double, // 경도
) {
    fun toDomain(): Stadium =
        Stadium(
            id = id,
            fullName = fullName,
            shortName = shortName,
            location = location,
            coordinate =
                Coordinate(
                    latitude = Latitude(latitude),
                    longitude = Longitude(longitude),
                ),
        )
}
