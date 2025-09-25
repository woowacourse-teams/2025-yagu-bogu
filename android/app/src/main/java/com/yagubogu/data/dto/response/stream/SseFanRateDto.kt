package com.yagubogu.data.dto.response.stream

import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SseFanRateDto(
    @SerialName("gameId")
    val gameId: Long,
    @SerialName("homeTeam")
    val homeTeam: SseTeamDto,
    @SerialName("awayTeam")
    val awayTeam: SseTeamDto,
) {
    fun toPresentation(): StadiumFanRateItem =
        StadiumFanRateItem(
            awayTeamFanRate = awayTeam.toPresentation(),
            homeTeamFanRate = homeTeam.toPresentation(),
        )
}
