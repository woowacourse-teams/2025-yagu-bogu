package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatCountsResponse(
    @SerialName("winCounts")
    val winCounts: Int, // 승리 횟수
    @SerialName("drawCounts")
    val drawCounts: Int, // 무승부 횟수
    @SerialName("loseCounts")
    val loseCounts: Int, // 패배 횟수
    @SerialName("favoriteCheckInCounts")
    val favoriteCheckInCounts: Int, // 내 팀 직관 횟수
)
