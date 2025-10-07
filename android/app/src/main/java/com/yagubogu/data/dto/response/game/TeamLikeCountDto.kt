package com.yagubogu.data.dto.response.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamLikeCountDto(
    @SerialName("teamId")
    val teamId: String, // 팀을 구분하는 코드 (예: "SS", "LG", "OB").
    @SerialName("totalCount")
    val totalCount: Int, // 해당 팀이 받은 좋아요(like)의 총 개수. 좋아요가 없으면 0.
)
