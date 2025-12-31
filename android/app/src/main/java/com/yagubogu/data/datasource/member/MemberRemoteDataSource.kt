package com.yagubogu.data.datasource.member

import com.yagubogu.data.dto.request.member.MemberFavoriteRequest
import com.yagubogu.data.dto.request.member.MemberNicknameRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlCompleteRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlStartRequest
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.member.MemberProfileResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.util.safeKtorApiCall
import javax.inject.Inject

class MemberRemoteDataSource @Inject constructor(
    private val memberApiService: MemberApiService,
) : MemberDataSource {
    override suspend fun getMemberInfo(): Result<MemberInfoResponse> =
        safeKtorApiCall {
            memberApiService.getMemberInfo()
        }

    override suspend fun getNickname(): Result<MemberNicknameResponse> =
        safeKtorApiCall {
            memberApiService.getNickname()
        }

    override suspend fun updateNickname(nickname: String): Result<MemberNicknameResponse> =
        safeKtorApiCall {
            val request = MemberNicknameRequest(nickname)
            memberApiService.patchNickname(request)
        }

    override suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse> =
        safeKtorApiCall {
            memberApiService.getFavoriteTeam()
        }

    override suspend fun updateFavoriteTeam(teamCode: String): Result<MemberFavoriteResponse> =
        safeKtorApiCall {
            val request = MemberFavoriteRequest(teamCode)
            memberApiService.patchFavoriteTeam(request)
        }

    override suspend fun deleteMember(): Result<Unit> =
        safeKtorApiCall {
            memberApiService.deleteMember()
        }

    override suspend fun getBadges(): Result<BadgeResponse> =
        safeKtorApiCall {
            memberApiService.getBadges()
        }

    override suspend fun updateRepresentativeBadge(badgeId: Long): Result<Unit> =
        safeKtorApiCall {
            memberApiService.patchRepresentativeBadge(badgeId)
        }

    override suspend fun getPresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlStartResponse> =
        safeKtorApiCall {
            val request =
                PresignedUrlStartRequest(
                    contentType,
                    contentLength,
                )
            memberApiService.postPresignedUrl(request)
        }

    override suspend fun completeUploadProfileImage(key: String): Result<PresignedUrlCompleteResponse> =
        safeKtorApiCall {
            val request = PresignedUrlCompleteRequest(key)
            memberApiService.postCompleteUpload(request)
        }

    override suspend fun getMemberProfile(memberId: Long): Result<MemberProfileResponse> =
        safeKtorApiCall {
            memberApiService.getMemberProfile(memberId)
        }
}
