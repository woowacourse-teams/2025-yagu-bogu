package com.yagubogu.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import com.yagubogu.presentation.util.mapListIndexed
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.stats.my.model.StatsCounts
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.Year
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
    private val checkInRepository: CheckInRepository,
) : ViewModel() {
    private val _statsMyUiModel = MutableStateFlow(StatsMyUiModel())
    val statsMyUiModel: StateFlow<StatsMyUiModel> = _statsMyUiModel.asStateFlow()

    private val _averageStats = MutableStateFlow(AverageStats())
    val averageStats: StateFlow<AverageStats> = _averageStats.asStateFlow()

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

    fun fetchMyStats() {
        fetchMyAttendanceStats()
        fetchMyAverageStats()
    }

    fun fetchDetailStats() {
        fetchVsTeamStats()
        fetchStadiumVisitCounts()
    }

    fun toggleVsTeamStats() {
        _isVsTeamStatsExpanded.value = !_isVsTeamStatsExpanded.value
    }

    private fun fetchMyAttendanceStats(year: Int = LocalDate.now().year) {
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
                        winningPercentage = winRate.roundToInt(),
                        myTeam = myTeam,
                        luckyStadium = luckyStadium,
                    )
                _statsMyUiModel.value = statsMyUiModel
            } else {
                val errors: List<String> =
                    listOf(statsCountsResult, winRateResult, myTeamResult, luckyStadiumResult)
                        .filter { it.isFailure }
                        .mapNotNull { it.exceptionOrNull()?.message }
                Timber.w("API 호출 실패: ${errors.joinToString()}")
            }
        }
    }

    private fun fetchMyAverageStats() {
        viewModelScope.launch {
            statsRepository
                .getAverageStats()
                .map { it.toUiModel() }
                .onSuccess { averageStats: AverageStats ->
                    _averageStats.value = averageStats
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun fetchVsTeamStats(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val vsTeamStatsResult: Result<List<VsTeamStatItem>> =
                statsRepository
                    .getVsTeamStats(year)
                    .mapListIndexed { index: Int, item: OpponentWinRateTeamDto ->
                        item.toUiModel(rank = index + 1)
                    }
            vsTeamStatsResult
                .onSuccess { updatedVsTeamStats: List<VsTeamStatItem> ->
                    _vsTeamStatItems.value = updatedVsTeamStats
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun fetchStadiumVisitCounts(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val stadiumVisitCountsResult: Result<List<StadiumVisitCount>> =
                checkInRepository.getStadiumCheckInCounts(year).mapList { it.toUiModel() }
            stadiumVisitCountsResult
                .onSuccess { stadiumVisitCounts: List<StadiumVisitCount> ->
                    _stadiumVisitCounts.value =
                        stadiumVisitCounts.sortedByDescending { it.visitCounts }
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    companion object {
        val START_YEAR: Int = 2021
        val END_YEAR: Int = Year.now().value

        private const val DEFAULT_TEAM_STATS_COUNT = 5
    }
}
