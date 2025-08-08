package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.talks.ContentDto
import com.yagubogu.data.dto.response.talks.TalkResponse

interface TalksDataSource {
    suspend fun getTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalkResponse>

    suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<ContentDto>
}
