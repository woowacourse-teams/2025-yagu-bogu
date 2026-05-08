package com.yagubogu.data.dto.response.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VictoryFairyRankingResponse(
    @SerialName("topRankings")
    val topRankings: List<VictoryFairyRankingDto>,
    @SerialName("myRanking")
    val myRanking: VictoryFairyRankingDto,
    @SerialName("nextCursorId")
    val nextCursorId: Long?, // 다음 페이지 커서, 다음 페이지가 없으면 null
    @SerialName("hasNext")
    val hasNext: Boolean, // 다음 페이지 존재 여부
)
