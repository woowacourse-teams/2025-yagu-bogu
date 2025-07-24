package com.yagubogu.presentation.stats.my

data class MyStatsUiModel(
    val winCount: Int,
    val drawCount: Int,
    val loseCount: Int,
    val totalCount: Int,
    val winningPercentage: Int,
    val luckyStadium: String?,
) {
    val etcPercentage: Int get() = FULL_PERCENTAGE - winningPercentage

    companion object {
        private const val FULL_PERCENTAGE = 100
    }
}
