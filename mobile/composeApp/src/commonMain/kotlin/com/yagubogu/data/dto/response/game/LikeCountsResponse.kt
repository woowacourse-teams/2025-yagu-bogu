package com.yagubogu.data.dto.response.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeCountsResponse(
    @SerialName("gameId")
    val gameId: Long, // 대상 경기 id
    @SerialName("counts")
    val counts: List<TeamLikeCountDto>, // 해당 경기의 두 팀(홈/원정)에 대한 좋아요 정보 목록.
)
