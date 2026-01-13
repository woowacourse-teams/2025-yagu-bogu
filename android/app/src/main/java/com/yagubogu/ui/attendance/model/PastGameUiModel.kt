package com.yagubogu.ui.attendance.model

import com.yagubogu.domain.model.Team
import java.time.LocalTime

data class PastGameUiModel(
    val gameId: Long,
    val startAt: LocalTime,
    val stadiumName: String,
    val awayTeam: Team,
    val awayTeamName: String,
    val homeTeam: Team,
    val homeTeamName: String,
)
