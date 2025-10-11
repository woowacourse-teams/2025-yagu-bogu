package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VictoryFairyDto(
    @SerialName("ranking")
    val ranking: Int, // 승리 요정 랭킹
    @SerialName("score")
    val score: Int, // 승리 요정 점수
)
