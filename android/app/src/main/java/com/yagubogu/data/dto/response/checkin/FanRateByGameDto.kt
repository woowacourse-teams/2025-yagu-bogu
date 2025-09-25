package com.yagubogu.data.dto.response.checkin

import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanRateByGameDto(
    @SerialName("homeTeam")
    val homeTeam: TeamFanRateDto, // 홈 팀
    @SerialName("awayTeam")
    val awayTeam: TeamFanRateDto, // 어웨이 팀
) {
    fun toPresentation(): StadiumFanRateItem =
        StadiumFanRateItem(
            awayTeamFanRate = awayTeam.toPresentation(),
            homeTeamFanRate = homeTeam.toPresentation(),
        )
}
