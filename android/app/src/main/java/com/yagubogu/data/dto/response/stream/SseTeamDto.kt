package com.yagubogu.data.dto.response.stream

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.home.stadium.TeamFanRate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SseTeamDto(
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("code")
    val code: String, // 팀 코드
    @SerialName("fanRate")
    val fanRate: Double, // 팬 비율
) {
    fun toPresentation(): TeamFanRate =
        TeamFanRate(
            team = Team.getByCode(code),
            teamName = name,
            fanRate = fanRate,
        )
}
