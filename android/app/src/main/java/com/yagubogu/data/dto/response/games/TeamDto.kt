package com.yagubogu.data.dto.response.games

import com.yagubogu.domain.model.Team
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    @SerialName("id")
    val id: Int, // 팀 식별자
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("code")
    val code: String, // 팀 코드
) {
    fun toDomain(): Team = Team.getByCode(code)
}
