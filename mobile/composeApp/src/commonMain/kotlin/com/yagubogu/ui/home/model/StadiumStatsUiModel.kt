package com.yagubogu.ui.home.model

import com.yagubogu.ui.util.now
import kotlinx.datetime.LocalTime

data class StadiumStatsUiModel(
    val stadiumFanRates: List<StadiumFanRateItem> = emptyList(),
    val refreshTime: LocalTime = LocalTime.now(),
)
