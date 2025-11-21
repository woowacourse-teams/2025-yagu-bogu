package com.yagubogu.presentation.stats.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StatsRepository
import com.yagubogu.presentation.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class StatsMyViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _statsMyUiModel = MutableLiveData<StatsMyUiModel>()
    val statsMyUiModel: LiveData<StatsMyUiModel> get() = _statsMyUiModel

    private val _averageStats = MutableLiveData<AverageStats>()
    val averageStats: LiveData<AverageStats> = _averageStats

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchMyStats()
        fetchMyAverageStats()
    }

    private fun fetchMyStats(year: Int = LocalDate.now().year) {
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
