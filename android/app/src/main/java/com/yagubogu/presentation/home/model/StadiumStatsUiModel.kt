package com.yagubogu.presentation.home.model

import com.yagubogu.presentation.home.stadium.StadiumFanRateItem
import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumFanRates: List<StadiumFanRateItem> = emptyList(),
    val refreshTime: LocalTime = LocalTime.now(),
)
