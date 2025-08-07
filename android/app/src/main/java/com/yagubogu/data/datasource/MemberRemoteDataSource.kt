package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.MemberFavoriteRequest
import com.yagubogu.data.dto.response.MemberFavoriteResponse
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.util.safeApiCall
import com.yagubogu.domain.model.Team

class MemberRemoteDataSource(
    private val memberApiService: MemberApiService,
) : MemberDataSource {
    override suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse> =
        safeApiCall {
            memberApiService.getFavoriteTeam()
        }

    override suspend fun updateFavoriteTeam(team: Team): Result<MemberFavoriteResponse> =
        safeApiCall {
            val request = MemberFavoriteRequest(team.name)
            memberApiService.patchFavoriteTeam(request)
        }
}
