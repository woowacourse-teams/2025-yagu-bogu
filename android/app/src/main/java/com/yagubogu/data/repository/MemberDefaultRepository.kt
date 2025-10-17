package com.yagubogu.data.repository

import com.yagubogu.data.datasource.member.MemberDataSource
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import com.yagubogu.data.network.TokenManager
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.presentation.setting.MemberInfoItem
import com.yagubogu.presentation.setting.PresignedUrlCompleteItem
import com.yagubogu.presentation.setting.PresignedUrlItem

class MemberDefaultRepository(
    private val memberDataSource: MemberDataSource,
    private val tokenManager: TokenManager,
) : MemberRepository {
    private var cachedNickname: String? = null
    private var cachedFavoriteTeam: String? = null

    override suspend fun getMemberInfo(): Result<MemberInfoItem> =
        memberDataSource.getMemberInfo().map { memberInfoResponse: MemberInfoResponse ->
            cachedNickname = memberInfoResponse.nickname
            cachedFavoriteTeam = memberInfoResponse.favoriteTeam
            memberInfoResponse.toPresentation()
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
                val newNickname: String = memberNicknameResponse.nickname
                cachedNickname = newNickname
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

    override suspend fun updateFavoriteTeam(team: Team): Result<Unit> =
        memberDataSource
            .updateFavoriteTeam(team)
            .map { memberFavoriteResponse: MemberFavoriteResponse ->
                val newFavoriteTeam: String? = memberFavoriteResponse.favorite
                cachedFavoriteTeam = newFavoriteTeam
            }

    override suspend fun deleteMember(): Result<Unit> =
        memberDataSource.deleteMember().map {
            tokenManager.clearTokens()
        }

    override fun invalidateCache() {
        cachedNickname = null
        cachedFavoriteTeam = null
    }

    override suspend fun getPresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlItem> =
        memberDataSource
            .getPresignedUrl(
                contentType,
                contentLength,
            ).map { presignedUrlStartResponse: PresignedUrlStartResponse ->
                PresignedUrlItem(
                    presignedUrlStartResponse.key,
                    presignedUrlStartResponse.url,
                )
            }

    override suspend fun completeUploadProfileImage(key: String): Result<PresignedUrlCompleteItem> =
        memberDataSource
            .completeUploadProfileImage(key)
            .map { presignedUrlCompleteResponse: PresignedUrlCompleteResponse ->
                PresignedUrlCompleteItem(presignedUrlCompleteResponse.url)
            }
}
