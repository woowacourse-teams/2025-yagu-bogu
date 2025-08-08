package com.yagubogu.data.dto.response.games

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GamesResponse(
    @SerialName("games")
    val games: List<GameDto>,
)
