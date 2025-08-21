package com.yagubogu.data.dto.response.checkin

import com.yagubogu.presentation.stats.detail.StadiumVisitCount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumCheckInCountDto(
    @SerialName("id")
    val id: Int, // 구장 id
    @SerialName("location")
    val location: String, // 구장 지역
    @SerialName("checkInCounts")
    val checkInCounts: Int, // 구장 방문 횟수
) {
    fun toPresentation(): StadiumVisitCount =
        StadiumVisitCount(
            stadiumName = location,
            visitCounts = checkInCounts,
        )
}
