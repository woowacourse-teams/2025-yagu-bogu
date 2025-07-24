package com.yagubogu.data.dto.response

import com.yagubogu.domain.model.TeamOccupancyRate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsStadiumOccupancyRateResponse(
    @SerialName("teams")
    val teams: List<TeamDTO>, // 팀 정보(팀 ID, 팀 이름, 점유율)
) {
    fun toDomains(): List<TeamOccupancyRate> = teams.map { it.toDomain() }
}
