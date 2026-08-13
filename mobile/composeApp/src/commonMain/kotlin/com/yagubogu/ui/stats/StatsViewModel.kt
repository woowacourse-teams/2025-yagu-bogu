package com.yagubogu.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.stats.my.model.StatsCounts
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
import com.yagubogu.ui.util.mapList
import com.yagubogu.ui.util.now
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class StatsViewModel(
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
    private val checkInRepository: CheckInRepository,
) : ViewModel() {
    private val logger = Logger.withTag("StatsViewModel")

    private val _year = MutableStateFlow<Int?>(LocalDate.now().year)
    val year: StateFlow<Int?> = _year.asStateFlow()

    private val _statsMyUiModel: MutableStateFlow<StatsMyUiModel?> = MutableStateFlow(null)
    val statsMyUiModel: StateFlow<StatsMyUiModel?> = _statsMyUiModel.asStateFlow()

    private val _averageStats: MutableStateFlow<AverageStats?> = MutableStateFlow(null)
    val averageStats: StateFlow<AverageStats?> = _averageStats.asStateFlow()

    private val _isVsTeamStatsExpanded = MutableStateFlow(false)
    val isVsTeamStatsExpanded: StateFlow<Boolean> = _isVsTeamStatsExpanded.asStateFlow()

    private val _vsTeamStatItems = MutableStateFlow<List<VsTeamStatItem>>(emptyList())
    val vsTeamStatItems: StateFlow<List<VsTeamStatItem>> =
        combine(
            isVsTeamStatsExpanded,
            _vsTeamStatItems,
        ) { isExpanded: Boolean, vsTeamStats: List<VsTeamStatItem> ->
            if (!isExpanded) vsTeamStats.take(DEFAULT_TEAM_STATS_COUNT) else vsTeamStats
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _stadiumVisitCounts = MutableStateFlow<List<StadiumVisitCount>>(emptyList())
    val stadiumVisitCounts: StateFlow<List<StadiumVisitCount>> = _stadiumVisitCounts.asStateFlow()

    private var attendanceJob: Job? = null
    private var averageStatsJob: Job? = null
    private var vsTeamStatsJob: Job? = null
    private var stadiumVisitCountsJob: Job? = null

    fun fetchMyStats(year: Int?) {
        fetchMyAttendanceStats(year)
        fetchMyAverageStats(year)
    }

    fun fetchDetailStats(year: Int?) {
        fetchVsTeamStats(year)
        fetchStadiumVisitCounts(year)
    }

    fun updateYear(year: Int?) {
        if (_year.value == year) return
        _statsMyUiModel.value = null
        _averageStats.value = null
        _vsTeamStatItems.value = emptyList()
        _stadiumVisitCounts.value = emptyList()
        _year.value = year
    }

    fun toggleVsTeamStats() {
        _isVsTeamStatsExpanded.value = !_isVsTeamStatsExpanded.value
    }

    private fun fetchMyAttendanceStats(year: Int?) {
        attendanceJob?.cancel()
        attendanceJob =
            viewModelScope.launch {
                val statsCountsDeferred: Deferred<Result<StatsCounts>> =
                    async { statsRepository.getStatsCounts(year).map { it.toUiModel() } }
                val winRateDeferred: Deferred<Result<Double>> =
                    async { statsRepository.getStatsWinRate(year) }
                val myTeamDeferred: Deferred<Result<String?>> =
                    async { memberRepository.getFavoriteTeam() }
                val luckyStadiumDeferred: Deferred<Result<String?>> =
                    async { statsRepository.getLuckyStadiums(year) }

                val statsCountsResult: Result<StatsCounts> = statsCountsDeferred.await()
                val winRateResult: Result<Double> = winRateDeferred.await()
                val myTeamResult: Result<String?> = myTeamDeferred.await()
                val luckyStadiumResult: Result<String?> = luckyStadiumDeferred.await()

                if (statsCountsResult.isSuccess && winRateResult.isSuccess && myTeamResult.isSuccess && luckyStadiumResult.isSuccess) {
                    val statsCounts: StatsCounts = statsCountsResult.getOrThrow()
                    val winRate: Double = winRateResult.getOrThrow()
                    val myTeam: String? = myTeamResult.getOrThrow()
                    val luckyStadium: String? = luckyStadiumResult.getOrThrow()

                    val statsMyUiModel =
                        StatsMyUiModel(
                            winCount = statsCounts.winCounts,
                            drawCount = statsCounts.drawCounts,
                            loseCount = statsCounts.loseCounts,
                            totalCount = statsCounts.favoriteCheckInCounts,
                            winningPercentage = winRate.toFloat(),
                            myTeam = myTeam,
                            luckyStadium = luckyStadium,
                        )
                    _statsMyUiModel.value = statsMyUiModel
                } else {
                    val errors: List<String> =
                        listOf(statsCountsResult, winRateResult, myTeamResult, luckyStadiumResult)
                            .filter { it.isFailure }
                            .mapNotNull { it.exceptionOrNull()?.message }
                    logger.w { "API 호출 실패: ${errors.joinToString()}" }
                }
            }
    }

    private fun fetchMyAverageStats(year: Int?) {
        averageStatsJob?.cancel()
        averageStatsJob =
            viewModelScope.launch {
                statsRepository
                    .getAverageStats(year)
                    .map { it.toUiModel() }
                    .onSuccess { averageStats: AverageStats ->
                        _averageStats.value = averageStats
                    }.onFailure { exception: Throwable ->
                        logger.w(exception) { "API 호출 실패" }
                    }
            }
    }

    private fun fetchVsTeamStats(year: Int?) {
        vsTeamStatsJob?.cancel()
        vsTeamStatsJob =
            viewModelScope.launch {
                val vsTeamStatsResult: Result<List<VsTeamStatItem>> =
                    statsRepository
                        .getVsTeamStats(year)
                        .mapList { item: OpponentWinRateTeamDto ->
                            item.toUiModel()
                        }.map { list: List<VsTeamStatItem> ->
                            list.sortedWith(
                                compareBy<VsTeamStatItem> { it.rank } // 1순위: rank 오름차순 (작은 숫자가 먼저)
                                    .thenByDescending { it.totalCounts }, // 2순위: totalCounts 내림차순 (많은 순서로)
                            )
                        }
                vsTeamStatsResult
                    .onSuccess { updatedVsTeamStats: List<VsTeamStatItem> ->
                        _vsTeamStatItems.value = updatedVsTeamStats
                    }.onFailure { exception: Throwable ->
                        logger.w(exception) { "API 호출 실패" }
                    }
            }
    }

    private fun fetchStadiumVisitCounts(year: Int?) {
        stadiumVisitCountsJob?.cancel()
        stadiumVisitCountsJob =
            viewModelScope.launch {
                val stadiumVisitCountsResult: Result<List<StadiumVisitCount>> =
                    checkInRepository.getStadiumCheckInCounts(year).mapList { it.toUiModel() }
                stadiumVisitCountsResult
                    .onSuccess { stadiumVisitCounts: List<StadiumVisitCount> ->
                        _stadiumVisitCounts.value =
                            stadiumVisitCounts.sortedByDescending { it.visitCounts }
                    }.onFailure { exception: Throwable ->
                        logger.w(exception) { "API 호출 실패" }
                    }
            }
    }

    companion object {
        val YEAR_RANGE: IntRange = 2021..LocalDate.now().year

        private const val DEFAULT_TEAM_STATS_COUNT = 5
    }
}
