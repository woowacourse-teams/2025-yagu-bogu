package com.yagubogu.data.dto.request.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeDeltaDto(
    @SerialName("teamId")
    val teamId: Long, // 좋아요가 적용되는 팀 ID
    @SerialName("delta")
    val delta: Int, // 좋아요의 증감 수치.
)
