package com.yagubogu.data.dto.response.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumsWithGamesResponse(
    @SerialName("stadiums")
    val stadiums: List<StadiumWithGameDto>,
)
