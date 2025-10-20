package com.yagubogu.data.dto.response.stadium

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.presentation.home.model.Stadium
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumWithGameDto(
    @SerialName("name")
    val name: String, // 구장 이름
    @SerialName("latitude")
    val latitude: Double, // 위도
    @SerialName("longitude")
    val longitude: Double, // 경도
    @SerialName("games")
    val games: List<GameDto>, // 게임 목록 (1차전, 2차전 순서)
) {
    fun toPresentation(): Stadium =
        Stadium(
            name = name,
            coordinate =
                Coordinate(
                    latitude = Latitude(latitude),
                    longitude = Longitude(longitude),
                ),
            gameIds = games.map { it.gameId },
        )
}
