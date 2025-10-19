package com.yagubogu.data.dto.response.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    @SerialName("code")
    val code: String, // 팀 코드
    @SerialName("shortName")
    val shortName: String, // 팀 이름
)
