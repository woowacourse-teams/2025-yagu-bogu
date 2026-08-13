package com.yagubogu.data.dto.response.game

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDatesResponse(
    @SerialName("dates")
    val dates: List<LocalDate>,
)
