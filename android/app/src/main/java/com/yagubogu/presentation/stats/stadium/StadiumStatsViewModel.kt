package com.yagubogu.presentation.stats.stadium

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.R
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
                listOf(
                    TeamOccupancyStatus("dummy", R.color.white, 0.0),
                ),
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
                    val teamStatus: List<TeamOccupancyStatus> =
                        teamOccupancyRates.rates.map { teamOccupancyRate: TeamOccupancyRate ->
                            val currentTeam: Team = Team.getById(teamOccupancyRate.id)
                            TeamOccupancyStatus(
                                currentTeam.shortName,
                                currentTeam.color,
                                teamOccupancyRate.occupancyRate,
                            )
                        }
                    val refinedTeamStatus = refineTeamStatus(teamStatus)

                    _stadiumStatsUiModel.value =
                        StadiumStatsUiModel(
                            teamOccupancyRates.stadiumName,
                            refinedTeamStatus,
                        )
                }.onFailure { exception: Throwable ->
                    Log.e(TAG, "API 호출 실패", exception)
                }
        }
    }

    private fun refineTeamStatus(teamStatuses: List<TeamOccupancyStatus>): List<TeamOccupancyStatus> {
        if (teamStatuses.isEmpty()) return emptyList()
        val sortedTeamStatus = teamStatuses.sortedByDescending { it.percentage }

        return when {
            sortedTeamStatus.size <= MAX_LEGEND_TEAM_SIZE -> sortedTeamStatus
            else -> {
                val topLegendTeams = sortedTeamStatus.take(MAX_LEGEND_TEAM_SIZE)
                val remainingTeams = sortedTeamStatus.drop(MAX_LEGEND_TEAM_SIZE)
                val othersTotalPercentage = remainingTeams.sumOf { it.percentage }

                val othersTeamStatus =
                    TeamOccupancyStatus(
                        name = "기타",
                        teamColor = R.color.gray400,
                        percentage = othersTotalPercentage,
                    )
                topLegendTeams + othersTeamStatus
            }
        }
    }

    companion object {
        private const val DUMMY_STADIUM_ID = 2L // 잠실구장
        private const val MAX_LEGEND_TEAM_SIZE = 2
        private const val TAG = "StadiumStatsViewModel"
    }
}
