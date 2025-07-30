package com.yagubogu.data.dto.response

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.home.model.TeamOccupancyRate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamOccupancyRateDto(
    @SerialName("id")
    val id: Long, // 팀 ID
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("occupancyRate")
    val occupancyRate: Double, // 점유율
) {
    fun toPresentation(): TeamOccupancyRate =
        TeamOccupancyRate(
            team = Team.getById(id),
            occupancyRate = occupancyRate,
        )
}
