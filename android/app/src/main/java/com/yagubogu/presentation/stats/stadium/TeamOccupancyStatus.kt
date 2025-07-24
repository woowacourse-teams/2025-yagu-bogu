package com.yagubogu.presentation.stats.stadium

import androidx.annotation.ColorRes

data class TeamOccupancyStatus(
    val name: String,
    @ColorRes
    val teamColor: Int,
    val percentage: Int,
)
