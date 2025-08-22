package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsWinRateResponse(
    @SerialName("winRate")
    val winPercent: Double, // 승률(%)
)
