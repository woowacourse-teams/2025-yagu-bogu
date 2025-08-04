package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumsResponse(
    @SerialName("stadiums")
    val stadiums: List<StadiumDto>,
)
