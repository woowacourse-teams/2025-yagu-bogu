package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.MemberFavoriteResponse

interface MemberDataSource {
    suspend fun getFavoriteTeam(memberId: Long): Result<MemberFavoriteResponse>
}
