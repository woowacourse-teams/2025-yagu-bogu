package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanRateResponse(
    @SerialName("fanRateByGames")
    val fanRateByGames: List<FanRateByGameDto>,
)
