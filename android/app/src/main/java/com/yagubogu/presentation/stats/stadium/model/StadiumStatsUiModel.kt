package com.yagubogu.presentation.stats.stadium.model

import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumName: String,
    val teamOccupancyStatuses: List<TeamOccupancyStatus>,
    val refreshTime: LocalTime = LocalTime.now(),
) {
    val firstTeamStatus: TeamOccupancyStatus =
        teamOccupancyStatuses.getOrElse(FIRST_TEAM_INDEX) { DEFAULT_TEAM_OCCUPANCY_STATUS }
    val secondTeamStatus: TeamOccupancyStatus =
        teamOccupancyStatuses.getOrElse(SECOND_TEAM_INDEX) { DEFAULT_TEAM_OCCUPANCY_STATUS }

    val firstTeamBias = remapRange(firstTeamStatus.percentage)
    val secondTeamBias = remapRange(secondTeamStatus.percentage)

    private fun remapRange(value: Double): Double {
        val scalingFactor: Double = (FULL_PERCENTAGE - CHART_END_PADDING_SIZE * 2) / FULL_PERCENTAGE
        val percentResult: Double = CHART_END_PADDING_SIZE + value * scalingFactor
        return percentResult / FULL_PERCENTAGE
    }

    companion object {
        private val DEFAULT_TEAM_OCCUPANCY_STATUS = TeamOccupancyStatus(null, 0.0)

        private const val FULL_PERCENTAGE = 100.0
        private const val CHART_END_PADDING_SIZE = 28.0
        private const val FIRST_TEAM_INDEX = 0
        private const val SECOND_TEAM_INDEX = 1
    }
}
