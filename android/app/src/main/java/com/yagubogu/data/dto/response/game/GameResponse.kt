package com.yagubogu.data.dto.response.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameResponse(
    @SerialName("games")
    val games: List<GameWithCheckInDto>,
)
