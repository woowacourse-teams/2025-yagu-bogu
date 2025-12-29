package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamFanRateDto(
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("code")
    val code: String, // 팀 코드
    @SerialName("fanRate")
    val fanRate: Double, // 팬 비율
)
