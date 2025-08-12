package com.yagubogu.data.dto.response.checkIns

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInHistoriesResponse(
    @SerialName("checkInHistory")
    val checkInHistories: List<CheckInHistoryDto>,
)
