package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInDto(
    @SerialName("counts")
    val counts: Int, // 누적 직관 인증 횟수
    @SerialName("winRate")
    val winRate: String, // 직관 승률 (예: "75%")
)
