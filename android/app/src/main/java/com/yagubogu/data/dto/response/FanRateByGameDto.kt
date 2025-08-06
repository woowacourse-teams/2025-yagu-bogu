package com.yagubogu.data.dto.response

import com.yagubogu.presentation.home.model.StadiumFanRate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanRateByGameDto(
    @SerialName("homeTeam")
    val homeTeam: TeamFanRateDto, // 홈 팀
    @SerialName("awayTeam")
    val awayTeam: TeamFanRateDto, // 어웨이 팀
) {
    fun toPresentation(): StadiumFanRate =
        StadiumFanRate(
            awayTeamFanRate = awayTeam.toPresentation(),
            homeTeamFanRate = homeTeam.toPresentation(),
        )
}
