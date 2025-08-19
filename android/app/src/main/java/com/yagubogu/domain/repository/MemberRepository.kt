package com.yagubogu.domain.repository

import com.yagubogu.domain.model.Team

interface MemberRepository {
    suspend fun getNickname(): Result<String>

    suspend fun updateNickname(nickname: String): Result<Unit>

    suspend fun getFavoriteTeam(): Result<String?>

    suspend fun updateFavoriteTeam(team: Team): Result<Unit>

    suspend fun deleteMember(): Result<Unit>
}
