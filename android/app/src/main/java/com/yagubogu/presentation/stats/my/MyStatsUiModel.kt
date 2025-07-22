package com.yagubogu.presentation.stats.my

import kotlin.math.roundToInt

data class MyStatsUiModel(
    val winCount: Int,
    val drawCount: Int,
    val loseCount: Int,
) {
    val totalCount: Int get() = winCount + drawCount + loseCount
    val winningPercentage: Int get() = ((winCount.toFloat() / totalCount) * 100).roundToInt()
    val etcPercentage: Int get() = 100 - winningPercentage
}
