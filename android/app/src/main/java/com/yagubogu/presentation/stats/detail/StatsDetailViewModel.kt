package com.yagubogu.presentation.stats.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsDetailViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val checkInRepository: CheckInRepository,
) : ViewModel() {
    private val _isVsTeamStatsExpanded = MutableLiveData(false)
    val isVsTeamStatsExpanded: LiveData<Boolean> get() = _isVsTeamStatsExpanded

    private var vsTeamStatItems: List<VsTeamStatItem> = emptyList()

    private val _stadiumVisitCounts = MutableLiveData<List<StadiumVisitCount>>()
    val stadiumVisitCounts: LiveData<List<StadiumVisitCount>> get() = _stadiumVisitCounts

    private val _vsTeamStats: MutableLiveData<List<VsTeamStatItem>> =
        MediatorLiveData<List<VsTeamStatItem>>().apply {
            addSource(isVsTeamStatsExpanded) { value = updateVsTeamStats() }
        }
    val vsTeamStats: LiveData<List<VsTeamStatItem>> get() = _vsTeamStats

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

    private fun fetchVsTeamStats(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val vsTeamStatsResult: Result<List<VsTeamStatItem>> =
                statsRepository.getVsTeamStats(year)
            vsTeamStatsResult
                .onSuccess { updatedVsTeamStats: List<VsTeamStatItem> ->
                    vsTeamStatItems = updatedVsTeamStats
                    _vsTeamStats.value = updateVsTeamStats()
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

    private fun updateVsTeamStats(): List<VsTeamStatItem> {
        val isExpanded: Boolean = isVsTeamStatsExpanded.value ?: false
        return if (!isExpanded) vsTeamStatItems.take(DEFAULT_TEAM_STATS_COUNT) else vsTeamStatItems
    }

    companion object {
        private const val DEFAULT_TEAM_STATS_COUNT = 5
    }
}
