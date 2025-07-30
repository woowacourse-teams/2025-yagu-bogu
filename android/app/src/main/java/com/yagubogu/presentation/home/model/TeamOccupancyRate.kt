package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Team

data class TeamOccupancyRate(
    val team: Team,
    val occupancyRate: Double,
)
