package com.yagubogu.data.dto.response.stadium

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.presentation.home.model.Stadium
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumWithGameDto(
    @SerialName("shortName")
    val shortName: String, // 구장 별명
    @SerialName("location")
    val location: String, // 위치
    @SerialName("latitude")
    val latitude: Double, // 위도
    @SerialName("longitude")
    val longitude: Double, // 경도
    @SerialName("homeTeam")
    val homeTeam: TeamDto, // 홈 팀
    @SerialName("awayTeam")
    val awayTeam: TeamDto, // 어웨이 팀
    @SerialName("games")
    val games: List<GameDto>, // 게임 목록 (1차전, 2차전 순서)
) {
    fun toPresentation(): Stadium =
        Stadium(
            shortName = shortName,
            location = location,
            coordinate =
                Coordinate(
                    latitude = Latitude(latitude),
                    longitude = Longitude(longitude),
                ),
            games = games.map { it.gameId },
        )
}
