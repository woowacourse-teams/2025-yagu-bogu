package com.yagubogu.presentation.stats.my

data class StatsMyUiModel(
    val winCount: Int = 0,
    val drawCount: Int = 0,
    val loseCount: Int = 0,
    val totalCount: Int = 0,
    val winningPercentage: Int = 0,
    val myTeam: String? = null,
    val luckyStadium: String? = null,
) {
    val etcPercentage: Int get() = FULL_PERCENTAGE - winningPercentage

    companion object {
        private const val FULL_PERCENTAGE = 100
    }
}
