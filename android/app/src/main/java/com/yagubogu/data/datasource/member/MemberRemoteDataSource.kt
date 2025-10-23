package com.yagubogu.data.datasource.member

import com.yagubogu.data.dto.request.member.MemberFavoriteRequest
import com.yagubogu.data.dto.request.member.MemberNicknameRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlCompleteRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlStartRequest
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.util.safeApiCall
import com.yagubogu.domain.model.Team
import javax.inject.Inject

class MemberRemoteDataSource
    @Inject
    constructor(
        private val memberApiService: MemberApiService,
    ) : MemberDataSource {
        override suspend fun getMemberInfo(): Result<MemberInfoResponse> =
            safeApiCall {
                memberApiService.getMemberInfo()
            }

        override suspend fun getNickname(): Result<MemberNicknameResponse> =
            safeApiCall {
                memberApiService.getNickname()
            }

        override suspend fun updateNickname(nickname: String): Result<MemberNicknameResponse> =
            safeApiCall {
                val request = MemberNicknameRequest(nickname)
                memberApiService.patchNickname(request)
            }

        override suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse> =
            safeApiCall {
                memberApiService.getFavoriteTeam()
            }

        override suspend fun updateFavoriteTeam(team: Team): Result<MemberFavoriteResponse> =
            safeApiCall {
                val request = MemberFavoriteRequest(team.name)
                memberApiService.patchFavoriteTeam(request)
            }

        override suspend fun deleteMember(): Result<Unit> =
            safeApiCall {
                memberApiService.deleteMember()
            }

        override suspend fun getBadges(): Result<BadgeResponse> =
            safeApiCall {
                memberApiService.getBadges()
            }

        override suspend fun updateRepresentativeBadge(badgeId: Long): Result<Unit> =
            safeApiCall {
                memberApiService.patchRepresentativeBadge(badgeId)
            }

        override suspend fun getPresignedUrl(
            contentType: String,
            contentLength: Long,
        ): Result<PresignedUrlStartResponse> =
            safeApiCall {
                val request =
                    PresignedUrlStartRequest(
                        contentType,
                        contentLength,
                    )
                memberApiService.postPresignedUrl(request)
            }

        override suspend fun completeUploadProfileImage(key: String): Result<PresignedUrlCompleteResponse> =
            safeApiCall {
                val request = PresignedUrlCompleteRequest(key)
                memberApiService.postCompleteUpload(request)
            }
    }
