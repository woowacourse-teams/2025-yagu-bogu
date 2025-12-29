package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScoreBoardDto(
    @SerialName("id")
    val id: Long, // 스코어보드 ID
    @SerialName("runs")
    val runs: Int, // 점수
    @SerialName("hits")
    val hits: Int, // 안타 개수
    @SerialName("errors")
    val errors: Int, // 실책 개수
    @SerialName("basesOnBalls")
    val basesOnBalls: Int, // 사사구 개수
    @SerialName("inningScores")
    val inningScores: List<String>, // 이닝별 점수
)
