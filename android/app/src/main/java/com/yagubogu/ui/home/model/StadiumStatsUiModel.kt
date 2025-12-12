package com.yagubogu.ui.home.model

import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumFanRates: List<StadiumFanRateItem> = emptyList(),
    val refreshTime: LocalTime = LocalTime.now(),
)
