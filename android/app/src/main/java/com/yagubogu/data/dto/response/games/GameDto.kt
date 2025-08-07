package com.yagubogu.data.dto.response.games

import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    @SerialName("gameId")
    val gameId: Long, // 경기 (톡방) 아이디
    @SerialName("totalCheckIns")
    val totalCheckIns: Int, // 경기장에 인증한 사람 수
    @SerialName("isMyCheckIn")
    val isMyCheckIn: Boolean, // 인증 여부
    @SerialName("stadium")
    val stadiumDto: StadiumDto,
    @SerialName("homeTeam")
    val homeTeam: TeamDto,
    @SerialName("awayTeam")
    val awayTeam: TeamDto,
) {
    fun toPresentation(): LivetalkStadiumItem =
        LivetalkStadiumItem(
            gameId = gameId,
            stadiumName = stadiumDto.name,
            userCount = totalCheckIns,
            awayTeam = awayTeam.toDomain(),
            homeTeam = homeTeam.toDomain(),
            isVerified = isMyCheckIn,
        )
}
