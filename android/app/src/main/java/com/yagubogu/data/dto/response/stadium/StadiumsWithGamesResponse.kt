package com.yagubogu.data.dto.response.stadium

import com.yagubogu.presentation.home.model.StadiumsWithGames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumsWithGamesResponse(
    @SerialName("stadiums")
    val stadiums: List<StadiumWithGameDto>,
) {
    fun toPresentation(): StadiumsWithGames = StadiumsWithGames(values = stadiums.map { it.toPresentation() })
}
