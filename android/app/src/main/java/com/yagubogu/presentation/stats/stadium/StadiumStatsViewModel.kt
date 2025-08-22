package com.yagubogu.presentation.stats.stadium

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.presentation.stats.stadium.model.StadiumStatsUiModel
import com.yagubogu.presentation.stats.stadium.model.TeamOccupancyRate
import com.yagubogu.presentation.stats.stadium.model.TeamOccupancyRates
import com.yagubogu.presentation.stats.stadium.model.TeamOccupancyStatus
import kotlinx.coroutines.launch
import java.time.LocalDate

class StadiumStatsViewModel(
    private val statsRepository: StatsRepository,
) : ViewModel() {
    private val _stadiumStatsUiModel: MutableLiveData<StadiumStatsUiModel> = MutableLiveData()
    val stadiumStatsUiModel: LiveData<StadiumStatsUiModel> get() = _stadiumStatsUiModel

    init {
        _stadiumStatsUiModel.value =
            StadiumStatsUiModel(
                "로딩중",
                listOf(TeamOccupancyStatus(Team.LG, 0.0)),
            )

        val today = LocalDate.now()
        fetchStadiumStats(DUMMY_STADIUM_ID, today)
    }

    private fun fetchStadiumStats(
        stadiumId: Long,
        date: LocalDate,
    ) {
        viewModelScope.launch {
            val teamOccupancyRatesResult: Result<TeamOccupancyRates> =
                statsRepository.getTeamOccupancyRates(stadiumId, date)
            teamOccupancyRatesResult
                .onSuccess { teamOccupancyRates: TeamOccupancyRates ->
                    val teamOccupancyStatuses: List<TeamOccupancyStatus> =
                        teamOccupancyRates.rates.map { teamOccupancyRate: TeamOccupancyRate ->
                            val team: Team = Team.getById(teamOccupancyRate.teamId)
                            TeamOccupancyStatus(
                                team,
                                teamOccupancyRate.occupancyRate,
                            )
                        }

                    val refinedTeamStatuses: List<TeamOccupancyStatus> =
                        refineTeamStatus(teamOccupancyStatuses)
                    _stadiumStatsUiModel.value =
                        StadiumStatsUiModel(
                            teamOccupancyRates.stadiumName,
                            refinedTeamStatuses,
                        )
                }.onFailure { exception: Throwable ->
                    Log.e(TAG, "API 호출 실패", exception)
                }
        }
    }

    private fun refineTeamStatus(teamStatuses: List<TeamOccupancyStatus>): List<TeamOccupancyStatus> =
        if (teamStatuses.isEmpty()) {
            teamStatuses
        } else {
            when {
                teamStatuses.size <= MAX_LEGEND_TEAM_SIZE -> teamStatuses
                else -> {
                    val topLegendTeamStatues: List<TeamOccupancyStatus> =
                        teamStatuses.take(MAX_LEGEND_TEAM_SIZE)
                    val etcPercentage: Double =
                        FULL_PERCENTAGE - topLegendTeamStatues.sumOf { it.percentage }

                    val etcTeamStatus =
                        TeamOccupancyStatus(
                            team = null,
                            percentage = etcPercentage,
                        )
                    topLegendTeamStatues + etcTeamStatus
                }
            }
        }

    companion object {
        private const val DUMMY_STADIUM_ID = 2L // 잠실구장
        private const val MAX_LEGEND_TEAM_SIZE = 2
        private const val FULL_PERCENTAGE = 100
        private const val TAG = "StadiumStatsViewModel"
    }
}
