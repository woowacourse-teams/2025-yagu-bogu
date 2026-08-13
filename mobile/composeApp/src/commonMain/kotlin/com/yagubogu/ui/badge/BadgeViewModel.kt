package com.yagubogu.ui.badge

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.ui.badge.model.BadgeInfoUiModel
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.mapper.toUiModel
import kotlinx.coroutines.launch

class BadgeViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val logger = Logger.withTag("BadgeViewModel")

    var badgeUiState = mutableStateOf<BadgeUiState>(BadgeUiState.Loading)
        private set

    fun fetchBadges() {
        viewModelScope.launch {
            val badgeResult: Result<BadgeResponse> = memberRepository.getBadges()
            badgeResult
                .onSuccess { badgeResponse: BadgeResponse ->
                    val representativeBadge: BadgeUiModel? =
                        badgeResponse.representativeBadge?.toUiModel()
                    val badges: List<BadgeInfoUiModel> = badgeResponse.badges.map { it.toUiModel() }
                    badgeUiState.value = BadgeUiState.Success(representativeBadge, badges)
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "fetchBadges API 호출 실패" }
                }
        }
    }

    fun updateRepresentativeBadge(badgeId: Long) {
        viewModelScope.launch {
            val patchRepresentativeBadgeResult: Result<Unit> =
                memberRepository.updateRepresentativeBadge(badgeId)
            patchRepresentativeBadgeResult
                .onSuccess {
                    updateBadgeUiState(badgeId)
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "updateRepresentativeBadge API 호출 실패" }
                }
        }
    }

    private fun updateBadgeUiState(badgeId: Long) {
        val currentBadgeUiState: BadgeUiState = badgeUiState.value

        if (currentBadgeUiState is BadgeUiState.Success) {
            val selectedBadge: BadgeInfoUiModel? =
                currentBadgeUiState.badges.find { it.badge.id == badgeId }
            selectedBadge?.let { badgeInfo: BadgeInfoUiModel ->
                badgeUiState.value =
                    currentBadgeUiState.copy(representativeBadge = badgeInfo.badge)
            }
        }
    }
}
