package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VictoryFairyRankingResponse(
    @SerialName("topRankings")
    val topRankings: List<VictoryFairyRankingDto>,
    @SerialName("myRanking")
    val myRanking: VictoryFairyRankingDto,
)
