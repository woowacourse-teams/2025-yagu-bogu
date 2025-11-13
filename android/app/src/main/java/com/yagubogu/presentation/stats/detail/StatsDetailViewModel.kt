package com.yagubogu.presentation.stats.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsDetailViewModel
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
        private val checkInRepository: CheckInRepository,
    ) : ViewModel() {
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

        init {
            fetchAll()
        }

        fun fetchAll() {
            fetchVsTeamStats()
            fetchStadiumVisitCounts()
        }

        fun toggleVsTeamStats() {
            _isVsTeamStatsExpanded.value = !_isVsTeamStatsExpanded.value
        }

        private fun fetchVsTeamStats(year: Int = LocalDate.now().year) {
            viewModelScope.launch {
                val vsTeamStatsResult: Result<List<VsTeamStatItem>> =
                    statsRepository.getVsTeamStats(year)
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
                    checkInRepository.getStadiumCheckInCounts(year)
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
            private const val DEFAULT_TEAM_STATS_COUNT = 5
        }
    }
