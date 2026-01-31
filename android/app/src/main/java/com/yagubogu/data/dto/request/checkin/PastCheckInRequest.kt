package com.yagubogu.data.dto.request.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PastCheckInRequest(
    @SerialName("gameId")
    val gameId: Long,
)
