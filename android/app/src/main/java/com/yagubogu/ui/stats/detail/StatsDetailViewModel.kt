package com.yagubogu.ui.stats.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.stats.OpponentWinRateTeamDto
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import com.yagubogu.presentation.util.mapListIndexed
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import com.yagubogu.ui.stats.detail.model.VsTeamStatItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsDetailViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val checkInRepository: CheckInRepository,
    private val clock: Clock,
    kermitLogger: Logger,
) : ViewModel() {
    val logger = kermitLogger.withTag("StatsDetailViewModel")

    private val _isVsTeamStatsExpanded = MutableStateFlow(false)
    val isVsTeamStatsExpanded: StateFlow<Boolean> = _isVsTeamStatsExpanded.asStateFlow()

    private val _stadiumVisitCounts = MutableStateFlow<List<StadiumVisitCount>>(emptyList())
    val stadiumVisitCounts: StateFlow<List<StadiumVisitCount>> = _stadiumVisitCounts.asStateFlow()

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

    fun fetchAll() {
        fetchVsTeamStats()
        fetchStadiumVisitCounts()
    }

    fun toggleVsTeamStats() {
        _isVsTeamStatsExpanded.value = !_isVsTeamStatsExpanded.value
    }

    private fun fetchVsTeamStats(year: Int = LocalDate.now(clock).year) {
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
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    private fun fetchStadiumVisitCounts(year: Int = LocalDate.now(clock).year) {
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
        private const val DEFAULT_TEAM_STATS_COUNT = 5
    }
}
