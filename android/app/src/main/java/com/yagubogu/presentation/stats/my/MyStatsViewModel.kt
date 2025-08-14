package com.yagubogu.presentation.stats.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StatsRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

class MyStatsViewModel(
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _myStatsUiModel = MutableLiveData<MyStatsUiModel>()
    val myStatsUiModel: LiveData<MyStatsUiModel> get() = _myStatsUiModel

    private val _averageStats = MutableLiveData<AverageStats>()
    val averageStats: LiveData<AverageStats> = _averageStats

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchMyStats(YEAR)
        fetchMyAverageStats()
    }

    private fun fetchMyStats(year: Int) {
        viewModelScope.launch {
            val statsCountsDeferred: Deferred<Result<StatsCounts>> =
                async { statsRepository.getStatsCounts(year) }
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

                val myStatsUiModel =
                    MyStatsUiModel(
                        winCount = statsCounts.winCounts,
                        drawCount = statsCounts.drawCounts,
                        loseCount = statsCounts.loseCounts,
                        totalCount = statsCounts.favoriteCheckInCounts,
                        winningPercentage = winRate.roundToInt(),
                        myTeam = myTeam,
                        luckyStadium = luckyStadium,
                    )
                _myStatsUiModel.value = myStatsUiModel
            } else {
                val errors: List<String> =
                    listOf(statsCountsResult, winRateResult, luckyStadiumResult)
                        .filter { it.isFailure }
                        .mapNotNull { it.exceptionOrNull()?.message }
                Timber.w("API 호출 실패: ${errors.joinToString()}")
            }
        }
    }

    private fun fetchMyAverageStats() {
        viewModelScope.launch {
            val averageStats: Result<AverageStats> = statsRepository.getAverageStats()
            averageStats
                .onSuccess { averageStats: AverageStats ->
                    _averageStats.value = averageStats
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    companion object {
        private const val YEAR = 2025
    }
}
