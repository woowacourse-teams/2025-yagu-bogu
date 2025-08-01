package com.yagubogu.presentation.home.model

import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumOccupancyRates: List<TeamOccupancyRates>,
    val refreshTime: LocalTime = LocalTime.now(),
)
