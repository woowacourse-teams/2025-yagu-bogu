package com.yagubogu.data.dto.response.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpponentWinRateResponse(
    @SerialName("opponents")
    val opponents: List<OpponentWinRateTeamDto>,
)
