package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInHistoryResponse(
    @SerialName("checkInHistory")
    val checkInHistory: List<CheckInGameDto>,
)
