package com.yagubogu.data.dto.response

import com.yagubogu.domain.model.TeamOccupancyRate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    @SerialName("id")
    val id: Long, // 팀 ID
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("occupancyRate")
    val occupancyRate: Double, // 점유율
) {
    fun toDomain(): TeamOccupancyRate =
        TeamOccupancyRate(
            id = id,
            name = name,
            occupancyRate = occupancyRate,
        )
}
