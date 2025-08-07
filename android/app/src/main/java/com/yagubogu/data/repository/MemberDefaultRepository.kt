package com.yagubogu.data.repository

import com.yagubogu.data.datasource.MemberDataSource
import com.yagubogu.data.dto.response.MemberFavoriteResponse
import com.yagubogu.domain.repository.MemberRepository

class MemberDefaultRepository(
    private val memberDataSource: MemberDataSource,
) : MemberRepository {
    private var cachedFavoriteTeam: String? = null

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

    // TODO: 응원팀을 수정한다면 캐시된 데이터를 invalidate 해야 됨
    fun invalidateCache() {
        cachedFavoriteTeam = null
    }
}
