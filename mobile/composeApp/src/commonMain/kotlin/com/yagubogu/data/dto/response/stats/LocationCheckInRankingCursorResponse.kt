package com.yagubogu.data.dto.response.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationCheckInRankingCursorResponse(
    @SerialName("myRanking")
    val myRanking: LocationCheckInRankingDto,
    @SerialName("rankings")
    val rankings: List<LocationCheckInRankingDto>,
    @SerialName("nextCursorId")
    val nextCursorId: Long?, // 다음 페이지 커서, 다음 페이지가 없으면 null
    @SerialName("hasNext")
    val hasNext: Boolean, // 다음 페이지 존재 여부
)
