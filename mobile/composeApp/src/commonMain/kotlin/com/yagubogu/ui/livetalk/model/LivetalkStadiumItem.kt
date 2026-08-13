package com.yagubogu.ui.livetalk.model

import com.yagubogu.domain.model.Team

data class LivetalkStadiumItem(
    val gameId: Long,
    val stadiumId: Long,
    val stadiumName: String,
    val userCount: Long,
    val awayTeam: Team,
    val homeTeam: Team,
    val isVerified: Boolean,
    val weatherUiModel: WeatherUiModel? = null,
)
