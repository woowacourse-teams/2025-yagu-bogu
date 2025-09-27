package com.yagubogu.ui.badge

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.ui.badge.model.BadgeInfoUiModel
import kotlinx.coroutines.launch
import timber.log.Timber

class BadgeViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    var badgeUiState = mutableStateOf<BadgeUiState>(BadgeUiState.Loading)
        private set

    init {
        fetchBadges()
    }

    fun patchRepresentativeBadge(badgeId: Long) {
        viewModelScope.launch {
            val patchRepresentativeBadgeResult: Result<Unit> =
                memberRepository.patchRepresentativeBadge(badgeId)
            patchRepresentativeBadgeResult
                .onSuccess {
                    val currentBadgeUiState: BadgeUiState = badgeUiState.value

                    if (currentBadgeUiState is BadgeUiState.Success) {
                        val selectedBadge: BadgeInfoUiModel? = currentBadgeUiState.badges.find { it.badge.id == badgeId }
                        selectedBadge?.let { badgeInfo: BadgeInfoUiModel ->
                            badgeUiState.value =
                                currentBadgeUiState.copy(representativeBadge = badgeInfo.badge)
                        }
                    }
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    private fun fetchBadges() {
        viewModelScope.launch {
            val badgesResult: Result<BadgeUiState> = memberRepository.getBadges()
            badgesResult
                .onSuccess { badges: BadgeUiState ->
                    Timber.d("$badges")
                    badgeUiState.value = badges
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }
}
