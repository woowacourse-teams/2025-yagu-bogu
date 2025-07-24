package com.yagubogu.presentation.stats.stadium.model

import com.yagubogu.R
import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumName: String,
    val teamOccupancyStatuses: List<TeamOccupancyStatus>,
    val refreshTime: LocalTime = LocalTime.now(),
) {
    val firstTeam: TeamOccupancyStatus get() = teamOccupancyStatuses.getOrElse(FIRST_TEAM_INDEX) { DEFAULT_TEAM_OCCUPANCY_STATUS }
    val secondTeam: TeamOccupancyStatus get() = teamOccupancyStatuses.getOrElse(SECOND_TEAM_INDEX) { DEFAULT_TEAM_OCCUPANCY_STATUS }
    val thirdTeam: TeamOccupancyStatus get() = teamOccupancyStatuses.getOrElse(THIRD_TEAM_INDEX) { DEFAULT_TEAM_OCCUPANCY_STATUS }

    val showFirstLegend: Boolean get() = firstTeam != DEFAULT_TEAM_OCCUPANCY_STATUS
    val showSecondLegend: Boolean get() = secondTeam != DEFAULT_TEAM_OCCUPANCY_STATUS
    val showThirdLegend: Boolean get() = thirdTeam != DEFAULT_TEAM_OCCUPANCY_STATUS

    companion object {
        private val DEFAULT_TEAM_OCCUPANCY_STATUS = TeamOccupancyStatus("", R.color.white, 0.0)

        private const val FIRST_TEAM_INDEX = 0
        private const val SECOND_TEAM_INDEX = 1
        private const val THIRD_TEAM_INDEX = 2
    }
}
