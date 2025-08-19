package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.talks.TalkCursorResponse
import com.yagubogu.data.dto.response.talks.TalkDto

interface TalksDataSource {
    suspend fun getTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalkCursorResponse>

    suspend fun getLatestTalks(
        gameId: Long,
        after: Long?,
        limit: Int,
    ): Result<TalkCursorResponse>

    suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<TalkDto>

    suspend fun deleteTalks(
        gameId: Long,
        talkId: Long,
    ): Result<Unit>

    suspend fun reportTalks(talkId: Long): Result<Unit>
}
