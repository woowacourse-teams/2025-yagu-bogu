package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.domain.model.Team

interface MemberDataSource {
    suspend fun getMemberInfo(): Result<MemberInfoResponse>

    suspend fun getNickname(): Result<MemberNicknameResponse>

    suspend fun updateNickname(nickname: String): Result<MemberNicknameResponse>

    suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse>

    suspend fun updateFavoriteTeam(team: Team): Result<MemberFavoriteResponse>

    suspend fun deleteMember(): Result<Unit>
}
