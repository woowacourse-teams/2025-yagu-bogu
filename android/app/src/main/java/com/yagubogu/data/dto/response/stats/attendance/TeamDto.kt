package com.yagubogu.data.dto.response.stats.attendance

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.stats.attendance.TeamItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    @SerialName("code")
    val code: String,
    @SerialName("name")
    val name: String,
    @SerialName("score")
    val score: Int,
    @SerialName("isMyTeam")
    val isMyTeam: Boolean,
) {
    fun toPresentation(): TeamItem =
        TeamItem(
            team = Team.getByCode(code),
            name = name,
            score = score,
            isMyTeam = isMyTeam,
        )
}
