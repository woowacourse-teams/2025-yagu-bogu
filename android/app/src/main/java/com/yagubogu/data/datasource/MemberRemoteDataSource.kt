package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.MemberFavoriteResponse
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.util.safeApiCall

class MemberRemoteDataSource(
    private val memberApiService: MemberApiService,
) : MemberDataSource {
    override suspend fun getFavoriteTeam(memberId: Long): Result<MemberFavoriteResponse> =
        safeApiCall {
            memberApiService.getFavoriteTeam(memberId)
        }
}
