package com.yagubogu.presentation.home.stadium

data class StadiumFanRateItem(
    val awayTeamFanRate: TeamFanRate,
    val homeTeamFanRate: TeamFanRate,
) {
    val awayTeamPercentage: Double = calculatePercentage(awayTeamFanRate.fanRate)
    val homeTeamPercentage: Double = calculatePercentage(homeTeamFanRate.fanRate)

    val awayTeamChartRange: Double = remapToChartRange(awayTeamPercentage)
    val homeTeamChartRange: Double = remapToChartRange(homeTeamPercentage)

    private fun calculatePercentage(fanRate: Double): Double {
        val totalFanRate: Double = awayTeamFanRate.fanRate + homeTeamFanRate.fanRate
        return if (totalFanRate == 0.0) {
            FULL_PERCENTAGE / 2
        } else {
            fanRate / totalFanRate * FULL_PERCENTAGE
        }
    }

    private fun remapToChartRange(percentage: Double): Double {
        val scalingFactor: Double = (FULL_PERCENTAGE - CHART_END_PADDING_SIZE * 2) / FULL_PERCENTAGE
        val scaledRange: Double = CHART_END_PADDING_SIZE + percentage * scalingFactor
        return scaledRange / FULL_PERCENTAGE
    }

    companion object {
        private const val FULL_PERCENTAGE = 100.0
        private const val CHART_END_PADDING_SIZE = 28.0
    }
}
