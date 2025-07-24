package com.yagubogu.presentation.stats.my

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.StatsCounts
import com.yagubogu.domain.repository.StatsRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MyStatsViewModel(
    private val statsRepository: StatsRepository,
) : ViewModel() {
    private val _myStatsUiModel = MutableLiveData<MyStatsUiModel>()
    val myStatsUiModel: LiveData<MyStatsUiModel> get() = _myStatsUiModel

    init {
        fetchMyStats(1, 2025)
    }

    private fun fetchMyStats(
        memberId: Long,
        year: Int,
    ) {
        viewModelScope.launch {
            val statsCountsDeferred: Deferred<Result<StatsCounts>> =
                async { statsRepository.getStatsCounts(memberId, year) }
            val winRateDeferred: Deferred<Result<Double>> =
                async { statsRepository.getStatsWinRate(memberId, year) }
            val luckyStadiumDeferred: Deferred<Result<String?>> =
                async { statsRepository.getLuckyStadiums(memberId, year) }

            val statsCountsResult = statsCountsDeferred.await()
            val winRateResult = winRateDeferred.await()
            val luckyStadiumResult = luckyStadiumDeferred.await()

            if (statsCountsResult.isSuccess && winRateResult.isSuccess && luckyStadiumResult.isSuccess) {
                val statsCounts = statsCountsResult.getOrThrow()
                val winRate = winRateResult.getOrThrow()
                val luckyStadium = luckyStadiumResult.getOrThrow() ?: "기록 없음"

                val myStatsUiModel =
                    MyStatsUiModel(
                        winCount = statsCounts.winCounts,
                        drawCount = statsCounts.drawCounts,
                        loseCount = statsCounts.loseCounts,
                        totalCount = statsCounts.favoriteCheckInCounts,
                        winningPercentage = winRate.roundToInt(),
                        luckyStadium = luckyStadium,
                    )
                _myStatsUiModel.value = myStatsUiModel
            } else {
                val errors: List<String> =
                    listOf(statsCountsResult, winRateResult, luckyStadiumResult)
                        .filter { it.isFailure }
                        .mapNotNull { it.exceptionOrNull()?.message }
                Log.e(TAG, "API 호출 실패: ${errors.joinToString()}")
            }
        }
    }

    companion object {
        private const val TAG = "MyStatsViewModel"
    }
}
