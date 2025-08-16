package com.yagubogu.data.repository

import com.yagubogu.data.datasource.MemberDataSource
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.MemberRepository

class MemberDefaultRepository(
    private val memberDataSource: MemberDataSource,
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
}
