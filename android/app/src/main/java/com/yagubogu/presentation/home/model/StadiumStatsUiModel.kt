package com.yagubogu.presentation.home.model

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
    val thirdTeamStatus: TeamOccupancyStatus =
        teamOccupancyStatuses.getOrElse(THIRD_TEAM_INDEX) { DEFAULT_TEAM_OCCUPANCY_STATUS }

    val showFirstLegend: Boolean = firstTeamStatus != DEFAULT_TEAM_OCCUPANCY_STATUS
    val showSecondLegend: Boolean = secondTeamStatus != DEFAULT_TEAM_OCCUPANCY_STATUS
    val showThirdLegend: Boolean = thirdTeamStatus != DEFAULT_TEAM_OCCUPANCY_STATUS

    companion object {
        private val DEFAULT_TEAM_OCCUPANCY_STATUS = TeamOccupancyStatus(null, 0.0)

        private const val FIRST_TEAM_INDEX = 0
        private const val SECOND_TEAM_INDEX = 1
        private const val THIRD_TEAM_INDEX = 2
    }
}
