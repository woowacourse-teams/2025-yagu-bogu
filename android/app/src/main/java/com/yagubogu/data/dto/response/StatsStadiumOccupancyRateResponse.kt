package com.yagubogu.data.dto.response

import com.yagubogu.presentation.stats.stadium.TeamOccupancyRate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsStadiumOccupancyRateResponse(
    @SerialName("teams")
    val teams: List<TeamOccupancyRateDto>, // 팀 정보(팀 ID, 팀 이름, 점유율)
) {
    fun toDomains(): List<TeamOccupancyRate> = teams.map { it.toDomain() }
}
