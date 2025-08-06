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

    init {
        fetchAll()
    }

    fun fetchAll() {
        fetchMyStats(MEMBER_ID, YEAR)
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
            val myTeamDeferred: Deferred<Result<String>> =
                async { memberRepository.getFavoriteTeam(memberId) }
            val luckyStadiumDeferred: Deferred<Result<String?>> =
                async { statsRepository.getLuckyStadiums(memberId, year) }

            val statsCountsResult: Result<StatsCounts> = statsCountsDeferred.await()
            val winRateResult: Result<Double> = winRateDeferred.await()
            val myTeamResult: Result<String> = myTeamDeferred.await()
            val luckyStadiumResult: Result<String?> = luckyStadiumDeferred.await()

            if (statsCountsResult.isSuccess && winRateResult.isSuccess && myTeamResult.isSuccess && luckyStadiumResult.isSuccess) {
                val statsCounts: StatsCounts = statsCountsResult.getOrThrow()
                val winRate: Double = winRateResult.getOrThrow()
                val myTeam: String = myTeamResult.getOrThrow()
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

    companion object {
        private const val MEMBER_ID = 5009L
        private const val YEAR = 2025
    }
}
