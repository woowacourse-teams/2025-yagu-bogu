package com.yagubogu.presentation.stats.stadium

import com.yagubogu.R
import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumName: String,
    val teams: List<TeamStatus>,
) {
    val refreshTime: LocalTime get() = LocalTime.now()
    private val teamSize: Int get() = teams.size

    val firstTeam: TeamStatus get() = teams.getOrElse(0) { DEFAULT_TEAM_STATUS }
    val secondTeam: TeamStatus get() = teams.getOrElse(1) { DEFAULT_TEAM_STATUS }
    val thirdTeam: TeamStatus get() = teams.getOrElse(2) { DEFAULT_TEAM_STATUS }

    val showFirstLegend: Boolean get() = teamSize >= 1
    val showSecondLegend: Boolean get() = teamSize >= 2
    val showThirdLegend: Boolean get() = teamSize >= 3

    companion object {
        private val DEFAULT_TEAM_STATUS = TeamStatus("", R.color.white, 0)
    }
}
