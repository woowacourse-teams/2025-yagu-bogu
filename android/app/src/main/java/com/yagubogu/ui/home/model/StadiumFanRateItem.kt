package com.yagubogu.ui.home.model

data class StadiumFanRateItem(
    val gameId: Long,
    val awayTeamFanRate: TeamFanRate,
    val homeTeamFanRate: TeamFanRate,
) {
    val awayTeamPercentage: Double = calculatePercentage(awayTeamFanRate.fanRate)
    val homeTeamPercentage: Double = calculatePercentage(homeTeamFanRate.fanRate)

    private fun calculatePercentage(fanRate: Double): Double {
        val totalFanRate: Double = awayTeamFanRate.fanRate + homeTeamFanRate.fanRate
        return if (totalFanRate == 0.0) {
            FULL_PERCENTAGE / 2
        } else {
            fanRate / totalFanRate * FULL_PERCENTAGE
        }
    }

    companion object {
        private const val FULL_PERCENTAGE = 100.0
    }
}
