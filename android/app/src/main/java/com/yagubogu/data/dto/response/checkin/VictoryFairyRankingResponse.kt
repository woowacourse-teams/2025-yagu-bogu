package com.yagubogu.data.dto.response.checkin

import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VictoryFairyRankingResponse(
    @SerialName("topRankings")
    val topRankings: List<VictoryFairyRankingDto>,
    @SerialName("myRanking")
    val myRanking: VictoryFairyRankingDto,
) {
    fun toPresentation(): VictoryFairyRanking =
        VictoryFairyRanking(
            topRankings = topRankings.map { it.toPresentation() },
            myRanking = myRanking.toPresentation(),
        )
}
