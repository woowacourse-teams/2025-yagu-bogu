package com.yagubogu.presentation.home.model

data class StadiumFanRate(
    val awayTeamFanRate: TeamFanRate,
    val homeTeamFanRate: TeamFanRate,
) {
    val awayTeamBias: Double = remapRange(awayTeamFanRate.fanRate)
    val homeTeamBias: Double = remapRange(homeTeamFanRate.fanRate)

    private fun remapRange(value: Double): Double {
        val scalingFactor: Double = (FULL_PERCENTAGE - CHART_END_PADDING_SIZE * 2) / FULL_PERCENTAGE
        val percentResult: Double = CHART_END_PADDING_SIZE + value * scalingFactor
        return percentResult / FULL_PERCENTAGE
    }

    companion object {
        private const val FULL_PERCENTAGE = 100.0
        private const val CHART_END_PADDING_SIZE = 28.0
    }
}
