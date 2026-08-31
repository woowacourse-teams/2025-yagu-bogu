package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.game.TeamByGameDto
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.model.PastGameUiModel
import com.yagubogu.ui.livetalk.component.LIVE_GAME_STATE_SCHEDULED
import com.yagubogu.ui.livetalk.model.LivetalkStadiumUiModel
import kotlinx.datetime.LocalDate

fun GameWithCheckInDto.toLivetalkUiModel(): LivetalkStadiumUiModel =
    LivetalkStadiumUiModel(
        gameId = gameId,
        stadiumId = stadium.id,
        stadiumName = stadium.name,
        userCount = totalCheckIns,
        isVerified = isMyCheckIn,
        liveGameState = LIVE_GAME_STATE_SCHEDULED, // TODO
    )

fun GameWithCheckInDto.toAttendanceUiModel(date: LocalDate): PastGameUiModel =
    PastGameUiModel(
        gameId = gameId,
        date = date,
        startAt = startAt,
        stadiumName = stadium.name,
        awayTeam = awayTeam.toDomain(),
        awayTeamName = awayTeam.name,
        homeTeam = homeTeam.toDomain(),
        homeTeamName = homeTeam.name,
    )

fun TeamByGameDto.toDomain(): Team = Team.getByCode(code)
