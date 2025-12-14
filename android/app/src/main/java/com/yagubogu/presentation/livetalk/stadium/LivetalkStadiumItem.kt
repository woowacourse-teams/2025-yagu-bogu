package com.yagubogu.presentation.livetalk.stadium

import com.yagubogu.domain.model.Team

data class LivetalkStadiumItem(
    val gameId: Long,
    val stadiumName: String,
    val userCount: Int,
    val awayTeam: Team,
    val homeTeam: Team,
    val isVerified: Boolean,
)
