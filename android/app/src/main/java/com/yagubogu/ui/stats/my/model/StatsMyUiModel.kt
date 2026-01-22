package com.yagubogu.ui.stats.my.model

data class StatsMyUiModel(
    val winCount: Int = 0,
    val drawCount: Int = 0,
    val loseCount: Int = 0,
    val totalCount: Int = 0,
    val winningPercentage: Float = 0f,
    val myTeam: String? = null,
    val luckyStadium: String? = null,
) {
    val etcPercentage: Float get() = FULL_PERCENTAGE - winningPercentage

    companion object {
        private const val FULL_PERCENTAGE = 100f
    }
}
