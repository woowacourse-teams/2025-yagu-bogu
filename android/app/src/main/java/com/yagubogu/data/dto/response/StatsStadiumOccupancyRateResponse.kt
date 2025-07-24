package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsStadiumOccupancyRateResponse(
    @SerialName("teams")
    val teams: List<Team>, // 팀 정보(팀 ID, 팀 이름, 점유율)
)
