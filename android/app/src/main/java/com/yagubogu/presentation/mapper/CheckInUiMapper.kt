package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.presentation.stats.detail.StadiumVisitCount
import java.time.LocalDate

fun FanRateByGameDto.toUiModel(): StadiumFanRateItem =
    StadiumFanRateItem(
        gameId = gameId,
        awayTeamFanRate = awayTeam.toPresentation(),
        homeTeamFanRate = homeTeam.toPresentation(),
    )

fun CheckInGameDto.toUiModel(): AttendanceHistoryItem {
    val summary =
        AttendanceHistoryItem.Summary(
            id = checkInId,
            attendanceDate = LocalDate.parse(attendanceDate),
            stadiumName = stadiumFullName,
            awayTeam = awayTeam.toPresentation(homeTeam),
            homeTeam = homeTeam.toPresentation(awayTeam),
        )

    if (homeScoreBoard == null || awayScoreBoard == null || awayTeam.pitcher == null || homeTeam.pitcher == null) {
        return AttendanceHistoryItem.Canceled(summary = summary)
    }

    return AttendanceHistoryItem.Detail(
        summary = summary,
        awayTeamPitcher = awayTeam.pitcher,
        homeTeamPitcher = homeTeam.pitcher,
        awayTeamScoreBoard = awayScoreBoard.toPresentation(),
        homeTeamScoreBoard = homeScoreBoard.toPresentation(),
    )
}

fun StadiumCheckInCountDto.toUiModel(): StadiumVisitCount =
    StadiumVisitCount(
        location = location,
        visitCounts = checkInCounts,
    )
