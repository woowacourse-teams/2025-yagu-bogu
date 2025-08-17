package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.MemberFavoriteRequest
import com.yagubogu.data.dto.request.MemberLogoutRequest
import com.yagubogu.data.dto.request.MemberNicknameRequest
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.util.safeApiCall
import com.yagubogu.domain.model.Team

class MemberRemoteDataSource(
    private val memberApiService: MemberApiService,
) : MemberDataSource {
    override suspend fun getNickname(): Result<MemberNicknameResponse> =
        safeApiCall {
            memberApiService.getNickname()
        }

    override suspend fun updateNickname(nickname: String): Result<MemberNicknameResponse> =
        safeApiCall {
            val request = MemberNicknameRequest(nickname)
            memberApiService.patchNickname(request)
        }

    override suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse> =
        safeApiCall {
            memberApiService.getFavoriteTeam()
        }

    override suspend fun updateFavoriteTeam(team: Team): Result<MemberFavoriteResponse> =
        safeApiCall {
            val request = MemberFavoriteRequest(team.name)
            memberApiService.patchFavoriteTeam(request)
        }

    override suspend fun logout(refreshToken: String): Result<Unit> =
        safeApiCall {
            val request = MemberLogoutRequest(refreshToken)
            memberApiService.logout(request)
        }

    override suspend fun deleteMember(): Result<Unit> =
        safeApiCall {
            memberApiService.deleteMember()
        }
}
