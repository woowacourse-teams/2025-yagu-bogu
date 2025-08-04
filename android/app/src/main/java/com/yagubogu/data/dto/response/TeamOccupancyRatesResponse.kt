package com.yagubogu.data.dto.response

import com.yagubogu.presentation.stats.stadium.model.TeamOccupancyRates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamOccupancyRatesResponse(
    @SerialName("stadiumShortName")
    val stadiumShortName: String, // 구장 별명
    @SerialName("teams")
    val teams: List<TeamOccupancyRateDto>, // 팀 정보 (팀 ID, 팀 이름, 점유율)
) {
    fun toPresentation(): TeamOccupancyRates =
        TeamOccupancyRates(
            stadiumName = stadiumShortName,
            rates =
                teams.map { teamOccupancyRateDto: TeamOccupancyRateDto ->
                    teamOccupancyRateDto.toPresentation()
                },
        )
}
