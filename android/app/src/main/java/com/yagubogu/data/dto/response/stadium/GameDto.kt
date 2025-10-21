package com.yagubogu.data.dto.response.stadium

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    @SerialName("gameId")
    val gameId: Long, // 게임 id
)
