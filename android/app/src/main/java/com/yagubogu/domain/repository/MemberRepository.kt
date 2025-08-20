package com.yagubogu.domain.repository

import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.domain.model.Team

interface MemberRepository {
    suspend fun getMemberInfo(): Result<MemberInfoResponse>

    suspend fun getNickname(): Result<String>

    suspend fun updateNickname(nickname: String): Result<Unit>

    suspend fun getFavoriteTeam(): Result<String?>

    suspend fun updateFavoriteTeam(team: Team): Result<Unit>

    suspend fun deleteMember(): Result<Unit>
}
