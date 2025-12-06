package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumCheckInCountDto(
    @SerialName("id")
    val id: Long, // 구장 id
    @SerialName("location")
    val location: String, // 구장 지역
    @SerialName("checkInCounts")
    val checkInCounts: Int, // 구장 방문 횟수
)
