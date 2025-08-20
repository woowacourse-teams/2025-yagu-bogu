package com.yagubogu.presentation.stats.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yagubogu.domain.model.Team

class StatsDetailViewModel : ViewModel() {
    private val _isVsTeamStatsExpanded = MutableLiveData(false)
    val isVsTeamStatsExpanded: LiveData<Boolean> get() = _isVsTeamStatsExpanded

    private var vsTeamStatItems: List<VsTeamStatItem> = emptyList()

    private val _stadiumVisitCounts = MutableLiveData<List<StadiumVisitCount>>()
    val stadiumVisitCounts: LiveData<List<StadiumVisitCount>> get() = _stadiumVisitCounts

    val vsTeamStats: LiveData<List<VsTeamStatItem>> =
        MediatorLiveData<List<VsTeamStatItem>>().apply {
            addSource(isVsTeamStatsExpanded) { value = updateVsTeamStats() }
        }

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchVsTeamStats()
        fetchStadiumVisitCounts()
    }

    fun toggleVsTeamStats() {
        _isVsTeamStatsExpanded.value = isVsTeamStatsExpanded.value?.not() ?: true
    }

    private fun fetchVsTeamStats() {
        // TODO: API 연결 후 DUMMY 제거
        vsTeamStatItems = DUMMY_VS_TEAM_STATS
    }

    private fun fetchStadiumVisitCounts() {
        // TODO: API 연결 후 DUMMY 제거
        _stadiumVisitCounts.value = DUMMY_STADIUM_VISIT_COUNTS
    }

    private fun updateVsTeamStats(): List<VsTeamStatItem> {
        val isExpanded: Boolean = isVsTeamStatsExpanded.value ?: false
        return if (!isExpanded) vsTeamStatItems.take(DEFAULT_TEAM_STATS_COUNT) else vsTeamStatItems
    }

    companion object {
        private const val DEFAULT_TEAM_STATS_COUNT = 5

        private val DUMMY_VS_TEAM_STATS =
            listOf(
                VsTeamStatItem(
                    rank = 1,
                    teamName = "두산",
                    team = Team.OB,
                    winCounts = 4,
                    drawCounts = 0,
                    loseCounts = 0,
                    winningPercentage = 100.0,
                ),
                VsTeamStatItem(
                    rank = 2,
                    teamName = "LG",
                    team = Team.LG,
                    winCounts = 3,
                    drawCounts = 1,
                    loseCounts = 0,
                    winningPercentage = 75.0,
                ),
                VsTeamStatItem(
                    rank = 3,
                    teamName = "키움",
                    team = Team.WO,
                    winCounts = 2,
                    drawCounts = 0,
                    loseCounts = 1,
                    winningPercentage = 66.6,
                ),
                VsTeamStatItem(
                    rank = 4,
                    teamName = "KT",
                    team = Team.KT,
                    winCounts = 2,
                    drawCounts = 0,
                    loseCounts = 2,
                    winningPercentage = 50.0,
                ),
                VsTeamStatItem(
                    rank = 5,
                    teamName = "삼성",
                    team = Team.SS,
                    winCounts = 1,
                    drawCounts = 0,
                    loseCounts = 2,
                    winningPercentage = 33.3,
                ),
                VsTeamStatItem(
                    rank = 6,
                    teamName = "NC",
                    team = Team.NC,
                    winCounts = 1,
                    drawCounts = 1,
                    loseCounts = 2,
                    winningPercentage = 25.0,
                ),
                VsTeamStatItem(
                    rank = 7,
                    teamName = "롯데",
                    team = Team.LT,
                    winCounts = 1,
                    drawCounts = 0,
                    loseCounts = 4,
                    winningPercentage = 20.0,
                ),
                VsTeamStatItem(
                    rank = 8,
                    teamName = "SSG",
                    team = Team.SK,
                    winCounts = 0,
                    drawCounts = 0,
                    loseCounts = 0,
                    winningPercentage = 0.0,
                ),
                VsTeamStatItem(
                    rank = 9,
                    teamName = "한화",
                    team = Team.HH,
                    winCounts = 0,
                    drawCounts = 0,
                    loseCounts = 0,
                    winningPercentage = 0.0,
                ),
            )

        private val DUMMY_STADIUM_VISIT_COUNTS =
            listOf(
                StadiumVisitCount("잠실", 30),
                StadiumVisitCount("인천", 4),
                StadiumVisitCount("고척", 4),
                StadiumVisitCount("수원", 3),
                StadiumVisitCount("대구", 2),
                StadiumVisitCount("부산", 2),
                StadiumVisitCount("수원", 1),
                StadiumVisitCount("대전", 0),
                StadiumVisitCount("광주", 0),
            )
    }
}
