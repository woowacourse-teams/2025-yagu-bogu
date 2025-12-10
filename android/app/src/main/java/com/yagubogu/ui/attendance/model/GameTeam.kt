package com.yagubogu.ui.attendance.model

import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team

data class GameTeam(
    val team: Team,
    val name: String,
    val score: String,
    val isMyTeam: Boolean,
    val gameResult: GameResult,
)
