package com.yagubogu.data.repository.member

import com.yagubogu.data.datasource.member.MemberDataSource
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.member.MemberProfileResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import com.yagubogu.data.network.TokenManager
import javax.inject.Inject

class MemberDefaultRepository @Inject constructor(
    private val memberDataSource: MemberDataSource,
    private val tokenManager: TokenManager,
) : MemberRepository {
    private var cachedNickname: String? = null
    private var cachedFavoriteTeam: String? = null

    override suspend fun getMemberInfo(): Result<MemberInfoResponse> =
        memberDataSource.getMemberInfo().map { memberInfoResponse: MemberInfoResponse ->
            cachedNickname = memberInfoResponse.nickname
            cachedFavoriteTeam = memberInfoResponse.favoriteTeam
            memberInfoResponse
        }

    override suspend fun getNickname(): Result<String> {
        cachedNickname?.let { nickname: String ->
            return Result.success(nickname)
        }

        return memberDataSource
            .getNickname()
            .map { memberNicknameResponse: MemberNicknameResponse ->
                val nickname: String = memberNicknameResponse.nickname
                cachedNickname = nickname
                nickname
            }
    }

    override suspend fun updateNickname(nickname: String): Result<Unit> =
        memberDataSource
            .updateNickname(nickname)
            .map { memberNicknameResponse: MemberNicknameResponse ->
                cachedNickname = memberNicknameResponse.nickname
            }.onFailure { exception ->
                val domainError = mapToNicknameUpdateError(exception)
                return Result.failure(NicknameUpdateException(domainError, exception))
            }

    override suspend fun getFavoriteTeam(): Result<String?> {
        cachedFavoriteTeam?.let { favoriteTeam: String ->
            return Result.success(favoriteTeam)
        }

        return memberDataSource
            .getFavoriteTeam()
            .map { memberFavoriteResponse: MemberFavoriteResponse ->
                val favoriteTeam: String? = memberFavoriteResponse.favorite
                cachedFavoriteTeam = favoriteTeam
                favoriteTeam
            }
    }

    override suspend fun updateFavoriteTeam(teamCode: String): Result<Unit> =
        memberDataSource
            .updateFavoriteTeam(teamCode)
            .map { memberFavoriteResponse: MemberFavoriteResponse ->
                val newFavoriteTeam: String? = memberFavoriteResponse.favorite
                cachedFavoriteTeam = newFavoriteTeam
            }

    override suspend fun deleteMember(): Result<Unit> =
        memberDataSource.deleteMember().map {
            tokenManager.clearTokens()
        }

    override suspend fun getBadges(): Result<BadgeResponse> = memberDataSource.getBadges()

    override suspend fun updateRepresentativeBadge(badgeId: Long): Result<Unit> = memberDataSource.updateRepresentativeBadge(badgeId)

    override fun invalidateCache() {
        cachedNickname = null
        cachedFavoriteTeam = null
    }

    override suspend fun getPresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlStartResponse> = memberDataSource.getPresignedUrl(contentType, contentLength)

    override suspend fun completeUploadProfileImage(key: String): Result<PresignedUrlCompleteResponse> =
        memberDataSource.completeUploadProfileImage(key)

    override suspend fun getMemberProfile(memberId: Long): Result<MemberProfileResponse> = memberDataSource.getMemberProfile(memberId)

    private fun mapToNicknameUpdateError(exception: Throwable): NicknameUpdateError {
        val message = exception.message.orEmpty()
        val exceptionString = exception.toString()
        return when {
            // 409 Conflict - 중복된 닉네임
            message.contains("409") || exceptionString.contains("Conflict") ->
                NicknameUpdateError.DuplicateNickname

            // 400 Bad Request, 422 Unprocessable Entity - 잘못된 형식
            message.contains("400") ||
                message.contains("422") ||
                exceptionString.contains("Bad Request") ||
                exceptionString.contains("Unprocessable") ->
                NicknameUpdateError.InvalidNickname

            // 403 Forbidden - 권한 없음
            message.contains("403") || exceptionString.contains("Forbidden") ->
                NicknameUpdateError.NoPermission

            // 404 Not Found - 회원 정보 없음
            message.contains("404") || exceptionString.contains("Not Found") ->
                NicknameUpdateError.MemberNotFound

            // 413 Payload Too Large - 데이터 크기 초과
            message.contains("413") || exceptionString.contains("Payload Too Large") ->
                NicknameUpdateError.PayloadTooLarge

            // 500 Internal Server Error, 502 Bad Gateway - 서버 에러
            message.contains("500") ||
                message.contains("502") ||
                exceptionString.contains("Internal Server Error") ||
                exceptionString.contains("Bad Gateway") ->
                NicknameUpdateError.ServerError

            // 기타 에러
            else -> NicknameUpdateError.Unknown(exception.message)
        }
    }
}
