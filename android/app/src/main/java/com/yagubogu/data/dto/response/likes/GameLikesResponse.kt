package com.yagubogu.data.dto.response.likes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameLikesResponse(
    @SerialName("gameId")
    val gameId: Long,
    @SerialName("counts")
    val counts: List<TeamLikeCount>,
)
