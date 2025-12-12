package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInGameDto(
    @SerialName("checkInId")
    val checkInId: Long, // 직관 ID
    @SerialName("stadiumFullName")
    val stadiumFullName: String, // 경기장 전체 이름
    @SerialName("homeTeam")
    val homeTeam: CheckInGameTeamDto,
    @SerialName("awayTeam")
    val awayTeam: CheckInGameTeamDto,
    @SerialName("attendanceDate")
    val attendanceDate: String, // 직관 날짜 (예시 "2025-04-05")
    @SerialName("homeScoreBoard")
    val homeScoreBoard: ScoreBoardDto?,
    @SerialName("awayScoreBoard")
    val awayScoreBoard: ScoreBoardDto?,
)
