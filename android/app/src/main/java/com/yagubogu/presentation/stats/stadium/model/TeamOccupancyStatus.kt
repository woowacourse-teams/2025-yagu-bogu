package com.yagubogu.presentation.stats.stadium.model

import androidx.annotation.ColorRes
import kotlin.math.roundToInt

data class TeamOccupancyStatus(
    val name: String,
    @ColorRes
    val teamColor: Int,
    val percentage: Double,
) {
    val roundedPercentage: Int
        get() = percentage.roundToInt()
}
