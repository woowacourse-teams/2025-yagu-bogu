package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PitcherRecordDto(
    @SerialName("playerName")
    val playerName: String, // 선수 이름
    @SerialName("result")
    val result: String, // 등판 결과
    @SerialName("innings")
    val innings: Double, // 투구 이닝 (예: 6.2 = 6 2/3이닝)
    @SerialName("pitchCount")
    val pitchCount: Int, // 총 투구 수
    @SerialName("hitsAllowed")
    val hitsAllowed: Int, // 피안타 수
    @SerialName("walksAndHbp")
    val walksAndHbp: Int, // 4사구 수
    @SerialName("strikeouts")
    val strikeouts: Int, // 삼진 수
    @SerialName("runsAllowed")
    val runsAllowed: Int, // 실점
    @SerialName("earnedRuns")
    val earnedRuns: Int, // 자책점
)
