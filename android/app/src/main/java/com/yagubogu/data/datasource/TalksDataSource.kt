package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.talks.ContentDto
import com.yagubogu.data.dto.response.talks.TalkResponse

interface TalksDataSource {
    suspend fun getTalks(
        token: String,
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalkResponse>

    suspend fun postTalks(
        token: String,
        gameId: Long,
        content: String,
    ): Result<ContentDto>
}
