package com.yagubogu.fixture

import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.presentation.setting.MemberInfoItem
import com.yagubogu.ui.badge.BadgeUiState
import com.yagubogu.ui.badge.model.BADGE_ID_0_ACQUIRED_FIXTURE_
import com.yagubogu.ui.badge.model.BadgeInfoUiModel

/**
 * @param isFailureMode `true`면 Result.failure를 반환
 */
class MemberFakeRepository(
    var isFailureMode: Boolean = false,
    private val badgeList: List<BadgeInfoUiModel> = emptyList(),
) : MemberRepository {
    override suspend fun getMemberInfo(): Result<MemberInfoItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getNickname(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun updateNickname(nickname: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getFavoriteTeam(): Result<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateFavoriteTeam(team: Team): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMember(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getBadges(): Result<BadgeUiState> =
        when (isFailureMode) {
            true -> {
                Result.failure(Exception())
            }

            false -> {
                Result.success(
                    BadgeUiState.Success(
                        BADGE_ID_0_ACQUIRED_FIXTURE_.badge,
                        badgeList,
                    ),
                )
            }
        }

    override suspend fun updateRepresentativeBadge(badgeId: Long): Result<Unit> =
        when (isFailureMode) {
            true -> {
                Result.failure(Exception())
            }

            false -> {
                Result.success(Unit)
            }
        }

    override fun invalidateCache() {
        TODO("Not yet implemented")
    }
}
