package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.game.TeamByGameDto
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.livetalk.model.LivetalkStadiumItem

fun GameWithCheckInDto.toUiModel(): LivetalkStadiumItem =
    LivetalkStadiumItem(
        gameId = gameId,
        stadiumName = stadium.name,
        userCount = totalCheckIns,
        awayTeam = awayTeam.toDomain(),
        homeTeam = homeTeam.toDomain(),
        isVerified = isMyCheckIn,
    )

fun TeamByGameDto.toDomain(): Team = Team.getByCode(code)
