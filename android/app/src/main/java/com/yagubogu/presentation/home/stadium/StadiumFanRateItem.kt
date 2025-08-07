package com.yagubogu.presentation.home.stadium

data class StadiumFanRateItem(
    val awayTeamFanRate: TeamFanRate,
    val homeTeamFanRate: TeamFanRate,
) {
    val awayTeamPercentage: Double =
        awayTeamFanRate.fanRate / (awayTeamFanRate.fanRate + homeTeamFanRate.fanRate) * FULL_PERCENTAGE
    val homeTeamPercentage: Double =
        homeTeamFanRate.fanRate / (awayTeamFanRate.fanRate + homeTeamFanRate.fanRate) * FULL_PERCENTAGE

    val awayTeamBias: Double = remapRange(awayTeamPercentage)
    val homeTeamBias: Double = remapRange(homeTeamPercentage)

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
