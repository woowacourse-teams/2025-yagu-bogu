package com.yagubogu.fixture

import com.yagubogu.data.dto.response.member.BadgeDto
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberProfileResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import com.yagubogu.data.repository.member.MemberRepository

/**
 * @param isFailureMode `true`면 Result.failure를 반환
 */
class MemberFakeRepository(
    var isFailureMode: Boolean = false,
    private val badgeList: List<BadgeDto> = emptyList(),
) : MemberRepository {
    override suspend fun getMemberInfo(): Result<MemberInfoResponse> {
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

    override suspend fun updateFavoriteTeam(teamCode: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMember(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getBadges(): Result<BadgeResponse> =
        when (isFailureMode) {
            true -> {
                Result.failure(Exception())
            }

            false -> {
                Result.success(
                    BadgeResponse(
                        REPRESENTATIVE_BADGE_FIXTURE,
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

    override suspend fun getPresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlStartResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun completeUploadProfileImage(key: String): Result<PresignedUrlCompleteResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberProfile(memberId: Long): Result<MemberProfileResponse> {
        TODO("Not yet implemented")
    }
}
