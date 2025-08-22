package com.yagubogu.data.dto.response

import com.yagubogu.domain.model.StatsCounts
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsCountsResponse(
    @SerialName("winCounts")
    val winCounts: Int, // 승리 횟수
    @SerialName("drawCounts")
    val drawCounts: Int, // 무승부 횟수
    @SerialName("loseCounts")
    val loseCounts: Int, // 패배 횟수
    @SerialName("favoriteCheckInCounts")
    val favoriteCheckInCounts: Int, // 내 팀 직관 횟수
) {
    fun toDomain() =
        StatsCounts(
            winCounts = winCounts,
            drawCounts = drawCounts,
            loseCounts = loseCounts,
            favoriteCheckInCounts = favoriteCheckInCounts,
        )
}
