package com.yagubogu.data.dto.response.checkin

import com.yagubogu.presentation.stats.attendance.AttendanceHistoryItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

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
) {
    fun toPresentation(): AttendanceHistoryItem =
        AttendanceHistoryItem(
            awayTeam = awayTeam.toPresentation(),
            homeTeam = homeTeam.toPresentation(),
            attendanceDate = LocalDate.parse(attendanceDate),
            stadiumName = stadiumFullName,
        )
}
