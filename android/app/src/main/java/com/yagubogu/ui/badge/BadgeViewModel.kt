package com.yagubogu.ui.badge

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.ui.badge.model.BADGE_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BADGE_NOT_ACQUIRED_FIXTURE
import com.yagubogu.ui.badge.model.BadgeUiModel
import kotlinx.coroutines.delay
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
                        val selectedBadge = currentBadgeUiState.badges.find { it.id == badgeId }
                        selectedBadge?.let { badge: BadgeUiModel ->
                            badgeUiState.value =
                                currentBadgeUiState.copy(representativeBadge = badge)
                        }
                    }
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    // TODO API 배포 시 코드 수정
    private fun fetchBadges() {
//        viewModelScope.launch {
//            val badgesResult: Result<BadgeUiState> = memberRepository.getBadges()
//            badgesResult
//                .onSuccess { badges: BadgeUiState ->
//                    Timber.d("$badges")
//                    badgeUiState.value = badges
//                }.onFailure { exception: Throwable ->
//                    Timber.w(exception, "API 호출 실패")
//                }
//        }
        viewModelScope.launch {
            delay(1000L)
            badgeUiState.value =
                BadgeUiState.Success(
                    BADGE_NOT_ACQUIRED_FIXTURE,
                    listOf(
                        BADGE_ACQUIRED_FIXTURE,
                        BADGE_NOT_ACQUIRED_FIXTURE,
                        BADGE_ACQUIRED_FIXTURE,
                    ),
                )
        }
    }
}
