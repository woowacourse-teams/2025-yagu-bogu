package com.yagubogu.ui.stats.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.stats.my.model.StatsCounts
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class StatsMyViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _scrollToTopEvent =
        MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()

    private val _statsMyUiModel = MutableStateFlow(StatsMyUiModel())
    val statsMyUiModel: StateFlow<StatsMyUiModel> = _statsMyUiModel.asStateFlow()

    private val _averageStats = MutableStateFlow(AverageStats())
    val averageStats: StateFlow<AverageStats> = _averageStats.asStateFlow()

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchMyStats()
        fetchMyAverageStats()
    }

    fun scrollToTop() {
        viewModelScope.launch {
            _scrollToTopEvent.emit(Unit)
        }
    }

    private fun fetchMyStats(year: Int = LocalDate.now(clock).year) {
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
}
