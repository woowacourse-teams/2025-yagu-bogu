package com.yagubogu.domain.repository

import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberProfileResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import com.yagubogu.domain.model.Team

interface MemberRepository {
    suspend fun getMemberInfo(): Result<MemberInfoResponse>

    suspend fun getNickname(): Result<String>

    suspend fun updateNickname(nickname: String): Result<Unit>

    suspend fun getFavoriteTeam(): Result<String?>

    suspend fun updateFavoriteTeam(team: Team): Result<Unit>

    suspend fun deleteMember(): Result<Unit>

    suspend fun getBadges(): Result<BadgeResponse>

    suspend fun updateRepresentativeBadge(badgeId: Long): Result<Unit>

    fun invalidateCache()

    suspend fun getPresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlStartResponse>

    suspend fun completeUploadProfileImage(key: String): Result<PresignedUrlCompleteResponse>

    suspend fun getMemberProfile(memberId: Long): Result<MemberProfileResponse>
}
