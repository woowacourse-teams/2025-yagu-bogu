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

    val firstTeamBias = remapRange(firstTeamStatus.percentage, CHART_END_PADDING_SIZE)
    val secondTeamBias = remapRange(secondTeamStatus.percentage, CHART_END_PADDING_SIZE)

    fun remapRange(
        value: Double,
        endPaddingSize: Double,
    ): Double {
        require(endPaddingSize >= 0.0 && endPaddingSize < 50.0) {
            "분할 차트의 양 끝 패딩 사이즈는 0.0에서 50.0사이의 값이 필요합니다"
        }
        val scalingFactor = ((100 - endPaddingSize) - endPaddingSize) / 100f
        val percentResult = endPaddingSize + value * scalingFactor

        return percentResult / 100.0
    }

    companion object {
        private val DEFAULT_TEAM_OCCUPANCY_STATUS = TeamOccupancyStatus(null, 0.0)

        private const val CHART_END_PADDING_SIZE = 20.0
        private const val FIRST_TEAM_INDEX = 0
        private const val SECOND_TEAM_INDEX = 1
    }
}
