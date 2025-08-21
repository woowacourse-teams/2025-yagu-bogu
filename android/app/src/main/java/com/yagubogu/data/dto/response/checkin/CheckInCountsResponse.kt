package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInCountsResponse(
    @SerialName("checkInCounts")
    val checkInCounts: Int, // 총 인증 횟수
)
