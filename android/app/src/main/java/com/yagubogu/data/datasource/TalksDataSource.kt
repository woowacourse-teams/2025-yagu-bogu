package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.talks.TalksResponse

interface TalksDataSource {
    suspend fun getTalks(
        token: String,
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalksResponse>
}
