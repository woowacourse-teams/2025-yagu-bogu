package com.yagubogu.data.dto.response.checkin

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
    val attendanceDate: LocalDate, // 경기 날짜
    @SerialName("startAt")
    val startAt: LocalTime, // 경기 시작 시간
    @SerialName("homeScoreBoard")
    val homeScoreBoard: ScoreBoardDto,
    @SerialName("awayScoreBoard")
    val awayScoreBoard: ScoreBoardDto,
    @SerialName("gameState")
    val gameState: String, // 경기 상태
)
