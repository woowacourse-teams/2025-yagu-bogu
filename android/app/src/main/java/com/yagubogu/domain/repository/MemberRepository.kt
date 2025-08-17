package com.yagubogu.domain.repository

import com.yagubogu.domain.model.Team

interface MemberRepository {
    suspend fun getNickname(): Result<String>

    suspend fun updateNickname(nickname: String): Result<String>

    suspend fun getFavoriteTeam(): Result<String>

    suspend fun updateFavoriteTeam(team: Team): Result<String>

    suspend fun logout(): Result<Unit>

    suspend fun deleteMember(): Result<Unit>
}
