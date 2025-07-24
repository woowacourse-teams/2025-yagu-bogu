package com.yagubogu.data.repository

import com.yagubogu.data.datasource.MemberDataSource
import com.yagubogu.data.dto.response.MemberFavoriteResponse
import com.yagubogu.domain.repository.MemberRepository

class MemberDefaultRepository(
    private val memberDataSource: MemberDataSource,
) : MemberRepository {
    override suspend fun getFavoriteTeam(memberId: Long): Result<String> =
        memberDataSource
            .getFavoriteTeam(memberId)
            .map { memberFavoriteResponse: MemberFavoriteResponse ->
                memberFavoriteResponse.favorite
            }
}
