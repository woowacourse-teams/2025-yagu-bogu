package com.yagubogu.presentation.home.model

import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumFanRates: List<StadiumFanRate>,
    val refreshTime: LocalTime = LocalTime.now(),
)
