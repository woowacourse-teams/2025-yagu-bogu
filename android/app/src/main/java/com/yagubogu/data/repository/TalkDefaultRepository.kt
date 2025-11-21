package com.yagubogu.data.repository

import com.yagubogu.data.datasource.talk.TalkDataSource
import com.yagubogu.data.dto.response.talk.TalkCursorResponse
import com.yagubogu.data.dto.response.talk.TalkEntranceResponse
import com.yagubogu.data.dto.response.talk.TalkResponse
import com.yagubogu.domain.repository.TalkRepository
import javax.inject.Inject

class TalkDefaultRepository @Inject constructor(
    private val talkDataSource: TalkDataSource,
) : TalkRepository {
    override suspend fun getBeforeTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalkCursorResponse> =
        talkDataSource.getTalks(
            gameId = gameId,
            before = before,
            limit = limit,
        )

    override suspend fun getAfterTalks(
        gameId: Long,
        after: Long?,
        limit: Int,
    ): Result<TalkCursorResponse> =
        talkDataSource.getLatestTalks(
            gameId = gameId,
            after = after,
            limit = limit,
        )

    override suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<TalkResponse> =
        talkDataSource.postTalks(
            gameId = gameId,
            content = content,
        )

    override suspend fun deleteTalks(
        gameId: Long,
        talkId: Long,
    ): Result<Unit> =
        talkDataSource.deleteTalks(
            gameId = gameId,
            talkId = talkId,
        )

    override suspend fun reportTalks(talkId: Long): Result<Unit> = talkDataSource.reportTalks(talkId = talkId)

    override suspend fun getInitial(gameId: Long): Result<TalkEntranceResponse> = talkDataSource.getInitial(gameId)
}
