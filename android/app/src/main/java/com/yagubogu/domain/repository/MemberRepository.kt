package com.yagubogu.domain.repository

import com.yagubogu.domain.model.Team

interface MemberRepository {
    suspend fun getFavoriteTeam(): Result<String?>

    suspend fun updateFavoriteTeam(team: Team): Result<Unit>
}
