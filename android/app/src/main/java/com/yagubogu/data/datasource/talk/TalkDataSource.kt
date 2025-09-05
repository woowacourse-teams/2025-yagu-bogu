package com.yagubogu.data.datasource.talk

import com.yagubogu.data.dto.response.talk.TalkCursorResponse
import com.yagubogu.data.dto.response.talk.TalkResponse

interface TalkDataSource {
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
    ): Result<TalkResponse>

    suspend fun deleteTalks(
        gameId: Long,
        talkId: Long,
    ): Result<Unit>

    suspend fun reportTalks(talkId: Long): Result<Unit>
}
