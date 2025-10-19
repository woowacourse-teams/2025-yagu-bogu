package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VictoryFairyDto(
    @SerialName("ranking")
    val ranking: Long?, // 승리 요정 랭킹
    @SerialName("rankWithinTeam")
    val rankWithinTeam: Long?, // 팀 별 승리 요정 랭킹
    @SerialName("score")
    val score: Double?, // 승리 요정 점수
)
