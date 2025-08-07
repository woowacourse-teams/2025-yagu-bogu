package com.yagubogu.data.dto.response.stats.attendance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInHistoriesResponse(
    @SerialName("checkInHistory")
    val checkInHistoryDto: List<CheckInHistoryDto>,
)
