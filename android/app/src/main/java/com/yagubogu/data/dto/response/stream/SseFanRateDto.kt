package com.yagubogu.data.dto.response.stream

import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SseFanRateDto(
    @SerialName("gameId")
    val gameId: Long, // 경기 ID
    @SerialName("homeTeam")
    val homeTeam: SseTeamDto, // 홈 팀
    @SerialName("awayTeam")
    val awayTeam: SseTeamDto, // 어웨이 팀
) {
    fun toPresentation(): StadiumFanRateItem =
        StadiumFanRateItem(
            gameId = gameId,
            awayTeamFanRate = awayTeam.toPresentation(),
            homeTeamFanRate = homeTeam.toPresentation(),
        )
}
