package com.yagubogu.presentation.stats.stadium

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.R
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.model.TeamOccupancyRate
import com.yagubogu.domain.repository.StatsRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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
            val statsStadiumOccupancyDeferred: Deferred<Result<List<TeamOccupancyRate>>> =
                async { statsRepository.getStatsStadiumOccupancyRate(stadiumId, date) }

            val stadiumOccupancyResult: Result<List<TeamOccupancyRate>> =
                statsStadiumOccupancyDeferred.await()

            if (stadiumOccupancyResult.isSuccess) {
                val teamOccupancyRates: List<TeamOccupancyRate> =
                    stadiumOccupancyResult.getOrThrow()

                val teamStatus: List<TeamOccupancyStatus> =
                    teamOccupancyRates.mapNotNull { teamOccupancyRate: TeamOccupancyRate ->
                        val currentTeam =
                            Team.getById(teamOccupancyRate.id) ?: return@mapNotNull null
                        currentTeam.let { team: Team ->
                            TeamOccupancyStatus(
                                team.shortName,
                                team.color,
                                teamOccupancyRate.occupancyRate,
                            )
                        }
                    }
                val refinedTeamStatus = refineTeamStatus(teamStatus)

                _stadiumStatsUiModel.value =
                    StadiumStatsUiModel(
                        "잠실구장",
                        refinedTeamStatus,
                    )
            } else {
                Log.e(TAG, "API 호출 실패: ${stadiumOccupancyResult.exceptionOrNull()?.message}")
            }
        }
    }

    private fun refineTeamStatus(teamStatuses: List<TeamOccupancyStatus>): List<TeamOccupancyStatus> {
        if (teamStatuses.isEmpty()) return emptyList()
        val sortedTeamStatus = teamStatuses.sortedByDescending { it.percentage }

        return when {
            sortedTeamStatus.size <= 2 -> sortedTeamStatus
            else -> {
                val topTwoTeams = sortedTeamStatus.take(2)
                val remainingTeams = sortedTeamStatus.drop(2)
                val othersTotalPercentage = remainingTeams.sumOf { it.percentage }

                val othersTeamStatus =
                    TeamOccupancyStatus(
                        name = "기타",
                        teamColor = R.color.gray400,
                        percentage = othersTotalPercentage,
                    )
                topTwoTeams + othersTeamStatus
            }
        }
    }

    companion object {
        private const val DUMMY_STADIUM_ID = 2L // 잠실구장
        private const val TAG = "StadiumStatsViewModel"
    }
}
