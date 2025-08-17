package com.yagubogu.data.repository

import com.yagubogu.data.datasource.MemberDataSource
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.network.TokenManager
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.MemberRepository

class MemberDefaultRepository(
    private val memberDataSource: MemberDataSource,
    private val tokenManager: TokenManager,
) : MemberRepository {
    private var cachedFavoriteTeam: String? = null
    private var cachedNickname: String? = null

    override suspend fun getNickname(): Result<String> =
        memberDataSource
            .getNickname()
            .map { memberNicknameResponse: MemberNicknameResponse ->
                val nickname = memberNicknameResponse.nickname
                cachedNickname = nickname
                nickname
            }

    override suspend fun updateNickname(nickname: String): Result<String> =
        memberDataSource
            .updateNickname(nickname)
            .map { memberNicknameResponse: MemberNicknameResponse ->
                val nickname = memberNicknameResponse.nickname
                cachedNickname = nickname
                nickname
            }

    override suspend fun getFavoriteTeam(): Result<String> {
        cachedFavoriteTeam?.let { favoriteTeam: String ->
            return Result.success(favoriteTeam)
        }
        return memberDataSource
            .getFavoriteTeam()
            .map { memberFavoriteResponse: MemberFavoriteResponse ->
                val favoriteTeam = memberFavoriteResponse.favorite
                cachedFavoriteTeam = favoriteTeam
                favoriteTeam
            }
    }

    override suspend fun updateFavoriteTeam(team: Team): Result<String> =
        memberDataSource
            .updateFavoriteTeam(team)
            .map { memberFavoriteResponse: MemberFavoriteResponse ->
                val favoriteTeam = memberFavoriteResponse.favorite
                cachedFavoriteTeam = favoriteTeam
                favoriteTeam
            }

    override suspend fun logout(): Result<Unit> {
        val refreshToken: String =
            tokenManager.getRefreshToken()
                ?: return Result.failure(Exception(ERROR_NO_REFRESH_TOKEN))

        return memberDataSource.logout(refreshToken).onSuccess {
            tokenManager.clearTokens()
        }
    }

    override suspend fun deleteMember(): Result<Unit> = memberDataSource.deleteMember()

    companion object {
        private const val ERROR_NO_REFRESH_TOKEN = "Refresh token is null"
    }
}
