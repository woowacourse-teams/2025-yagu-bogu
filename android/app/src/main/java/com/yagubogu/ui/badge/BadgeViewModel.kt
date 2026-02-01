package com.yagubogu.ui.badge

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.ui.badge.model.BadgeInfoUiModel
import com.yagubogu.ui.badge.model.BadgeUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BadgeViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
) : ViewModel() {
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
                    Timber.w(exception, "API 호출 실패")
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
                    Timber.w(exception, "API 호출 실패")
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
