package com.yagubogu.presentation.stats.stadium

import java.time.LocalTime

data class StadiumStatsUiModel(
    val stadiumName: String,
    val teams: List<TeamStatus>,
) {
    val refreshTime: LocalTime get() = LocalTime.now()
    val teamSize: Int get() = teams.size

    val firstTeam: TeamStatus? get() = teams.getOrNull(0)
    val secondTeam: TeamStatus? get() = teams.getOrNull(1)
    val thirdTeam: TeamStatus? get() = teams.getOrNull(2)

    val showFirstLegend: Boolean get() = teamSize >= 1
    val showSecondLegend: Boolean get() = teamSize >= 2
    val showThirdLegend: Boolean get() = teamSize >= 3
}
