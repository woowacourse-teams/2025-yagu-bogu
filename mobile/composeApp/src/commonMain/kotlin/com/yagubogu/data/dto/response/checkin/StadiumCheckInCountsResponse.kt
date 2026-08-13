package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumCheckInCountsResponse(
    @SerialName("stadiums")
    val stadiums: List<StadiumCheckInCountDto>,
)
