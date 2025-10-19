package com.yagubogu.data.dto.response.member

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInDto(
    @SerialName("counts")
    val counts: Int, // 누적 직관 인증 횟수
    @SerialName("winRate")
    val winRate: String, // 직관 승률 (예: "75%")
    @SerialName("winCounts")
    val winCounts: Int, // 직관 승리 횟수
    @SerialName("drawCounts")
    val drawCounts: Int, // 직관 무승부 횟수
    @SerialName("loseCounts")
    val loseCounts: Int, // 직관 패배 횟수
    @SerialName("recentCheckInDate")
    val recentCheckInDate: LocalDate, // 최근 직관 날짜
)
