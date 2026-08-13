package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanRateByGameDto(
    @SerialName("gameId")
    val gameId: Long, // 경기 ID
    @SerialName("homeTeam")
    val homeTeam: TeamFanRateDto, // 홈 팀
    @SerialName("awayTeam")
    val awayTeam: TeamFanRateDto, // 어웨이 팀
)
