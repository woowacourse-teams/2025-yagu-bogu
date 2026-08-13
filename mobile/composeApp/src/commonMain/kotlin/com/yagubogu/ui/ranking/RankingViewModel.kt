package com.yagubogu.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import com.yagubogu.ui.util.now
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class RankingViewModel(
    private val type: RankingType,
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("RankingViewModel")

    private val _rankingUiModel = MutableStateFlow(RankingUiModel(type))
    val rankingUiModel: StateFlow<RankingUiModel> = _rankingUiModel.asStateFlow()

    fun fetchRanking(year: Int = LocalDate.now(clock).year) {
        if (!rankingUiModel.value.hasNext || rankingUiModel.value.isLoading) return

        _rankingUiModel.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val rankingResult: Result<RankingUiModel> =
                when (type) {
                    RankingType.CHECK_IN ->
                        statsRepository
                            .getCheckInRankings(
                                year = year,
                                before = rankingUiModel.value.nextCursorId,
                                limit = RANKING_LIMIT,
                            ).map { it.toUiModel() }

                    RankingType.VICTORY_FAIRY ->
                        statsRepository
                            .getVictoryFairyRankings(
                                year = year,
                                teamCode = null,
                                before = rankingUiModel.value.nextCursorId,
                                limit = RANKING_LIMIT,
                            ).map { it.toUiModel() }
                }
            rankingResult
                .onSuccess { ranking: RankingUiModel ->
                    _rankingUiModel.update { current: RankingUiModel ->
                        current.copy(
                            topRankings = (current.topRankings + ranking.topRankings).toImmutableList(),
                            myRanking = ranking.myRanking,
                            nextCursorId = ranking.nextCursorId,
                            hasNext = ranking.hasNext,
                            isLoading = false, // 로딩 완료
                        )
                    }
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                    _rankingUiModel.update { it.copy(isLoading = false) }
                }
        }
    }

    fun fetchMemberProfile(memberId: Long) {
        viewModelScope.launch {
            val memberProfileResult: Result<MemberProfile> =
                memberRepository.getMemberProfile(memberId).map { it.toUiModel() }
            memberProfileResult
                .onSuccess { profile: MemberProfile ->
                    _rankingUiModel.update { it.copy(selectedMemberProfile = profile) }
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패 (fetchMemberProfile)" }
                }
        }
    }

    fun clearSelectedMemberProfile() {
        _rankingUiModel.update { it.copy(selectedMemberProfile = null) }
    }

    companion object {
        private const val RANKING_LIMIT = 25
    }
}
