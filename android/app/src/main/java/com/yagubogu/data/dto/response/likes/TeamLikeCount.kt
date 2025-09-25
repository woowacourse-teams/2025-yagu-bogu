package com.yagubogu.data.dto.response.likes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamLikeCount(
    @SerialName("teamId")
    val teamId: Long,
    @SerialName("likeCount")
    val likeCount: Int,
)
