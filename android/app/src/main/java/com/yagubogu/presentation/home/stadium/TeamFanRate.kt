package com.yagubogu.presentation.home.stadium

import com.yagubogu.domain.model.Team

data class TeamFanRate(
    val team: Team,
    val teamName: String,
    val fanRate: Double,
)
