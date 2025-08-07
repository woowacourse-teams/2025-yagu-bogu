package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.MemberFavoriteResponse
import com.yagubogu.domain.model.Team

interface MemberDataSource {
    suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse>

    suspend fun updateFavoriteTeam(team: Team): Result<MemberFavoriteResponse>
}
