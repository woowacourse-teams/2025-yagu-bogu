package com.yagubogu.data.dto.response.stadium

import com.yagubogu.presentation.home.model.Stadiums
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumsWithGamesResponse(
    @SerialName("stadiums")
    val stadiums: List<StadiumWithGameDto>,
) {
    fun toCheckInPresentation(): Stadiums = Stadiums(values = stadiums.map { it.toPresentation() })

    // TODO: 과거 직관 인증 toPastCheckInPresentation
}
