package com.yagubogu.data.repository

import com.yagubogu.data.datasource.MemberDataSource
import com.yagubogu.data.dto.response.MemberFavoriteResponse
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.MemberRepository

class MemberDefaultRepository(
    private val memberDataSource: MemberDataSource,
) : MemberRepository {
    private var cachedFavoriteTeam: String? = null

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
                val favoriteTeam: String? = memberFavoriteResponse.favorite
                cachedFavoriteTeam = favoriteTeam
            }
}
