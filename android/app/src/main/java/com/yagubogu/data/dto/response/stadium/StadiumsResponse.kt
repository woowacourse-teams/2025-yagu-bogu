package com.yagubogu.data.dto.response.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumsResponse(
    @SerialName("stadiums")
    val stadiums: List<StadiumDto>,
)
