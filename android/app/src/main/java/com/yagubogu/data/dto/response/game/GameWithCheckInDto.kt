package com.yagubogu.data.dto.response.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameWithCheckInDto(
    @SerialName("gameId")
    val gameId: Long, // 경기 아이디
    @SerialName("totalCheckIns")
    val totalCheckIns: Long, // 경기장에 인증한 사람 수
    @SerialName("isMyCheckIn")
    val isMyCheckIn: Boolean, // 인증 여부
    @SerialName("stadium")
    val stadium: StadiumByGameDto,
    @SerialName("homeTeam")
    val homeTeam: TeamByGameDto,
    @SerialName("awayTeam")
    val awayTeam: TeamByGameDto,
)
