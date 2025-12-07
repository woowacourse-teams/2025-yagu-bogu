package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.CheckInGameTeamDto
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.ScoreBoardDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.data.dto.response.checkin.TeamFanRateDto
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.GameScoreBoard
import com.yagubogu.presentation.attendance.model.GameTeam
import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import com.yagubogu.presentation.home.stadium.TeamFanRate
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import java.time.LocalDate

fun FanRateByGameDto.toUiModel(): StadiumFanRateItem =
    StadiumFanRateItem(
        gameId = gameId,
        awayTeamFanRate = awayTeam.toUiModel(),
        homeTeamFanRate = homeTeam.toUiModel(),
    )

fun TeamFanRateDto.toUiModel(): TeamFanRate =
    TeamFanRate(
        team = Team.getByCode(code),
        teamName = name,
        fanRate = fanRate,
    )

fun CheckInGameDto.toUiModel(): AttendanceHistoryItem {
    val summary =
        AttendanceHistoryItem.Summary(
            id = checkInId,
            attendanceDate = LocalDate.parse(attendanceDate),
            stadiumName = stadiumFullName,
            awayTeam = awayTeam.toUiModel(homeTeam),
            homeTeam = homeTeam.toUiModel(awayTeam),
        )

    if (homeScoreBoard == null || awayScoreBoard == null || awayTeam.pitcher == null || homeTeam.pitcher == null) {
        return AttendanceHistoryItem.Canceled(summary = summary)
    }

    return AttendanceHistoryItem.Detail(
        summary = summary,
        awayTeamPitcher = awayTeam.pitcher,
        homeTeamPitcher = homeTeam.pitcher,
        awayTeamScoreBoard = awayScoreBoard.toUiModel(),
        homeTeamScoreBoard = homeScoreBoard.toUiModel(),
    )
}

fun CheckInGameTeamDto.toUiModel(opponent: CheckInGameTeamDto): GameTeam =
    GameTeam(
        team = Team.getByCode(code),
        name = name,
        score = score?.toString() ?: "-",
        isMyTeam = isMyTeam,
        gameResult =
            if (score == null || opponent.score == null) {
                GameResult.DRAW
            } else {
                GameResult.from(score, opponent.score)
            },
    )

fun ScoreBoardDto.toUiModel(): GameScoreBoard =
    GameScoreBoard(
        runs = runs,
        hits = hits,
        errors = errors,
        basesOnBalls = basesOnBalls,
        scores = inningScores,
    )

fun StadiumCheckInCountDto.toUiModel(): StadiumVisitCount =
    StadiumVisitCount(
        location = location,
        visitCounts = checkInCounts,
    )
