package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.game.TeamByGameDto
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.PastGameUiModel
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem
import kotlinx.datetime.toJavaLocalTime
import java.time.LocalDate

fun GameWithCheckInDto.toLivetalkUiModel(): LivetalkStadiumItem =
    LivetalkStadiumItem(
        gameId = gameId,
        stadiumName = stadium.name,
        userCount = totalCheckIns,
        awayTeam = awayTeam.toDomain(),
        homeTeam = homeTeam.toDomain(),
        isVerified = isMyCheckIn,
    )

fun GameWithCheckInDto.toAttendanceUiModel(date: LocalDate): PastGameUiModel =
    PastGameUiModel(
        gameId = gameId,
        date = date,
        startAt = startAt.toJavaLocalTime(),
        stadiumName = stadium.name,
        awayTeam = awayTeam.toDomain(),
        awayTeamName = awayTeam.name,
        homeTeam = homeTeam.toDomain(),
        homeTeamName = homeTeam.name,
    )

fun TeamByGameDto.toDomain(): Team = Team.getByCode(code)
