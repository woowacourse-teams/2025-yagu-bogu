package com.yagubogu.data.repository

import com.yagubogu.data.datasource.talk.TalkDataSource
import com.yagubogu.data.dto.response.talk.TalkCursorResponse
import com.yagubogu.data.dto.response.talk.TalkResponse
import com.yagubogu.domain.repository.TalkRepository
import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem
import com.yagubogu.presentation.livetalk.chat.LivetalkResponseItem

class TalkDefaultRepository(
    private val talkDataSource: TalkDataSource,
) : TalkRepository {
    override suspend fun getBeforeTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<LivetalkResponseItem> =
        talkDataSource
            .getTalks(
                gameId = gameId,
                before = before,
                limit = limit,
            ).map { talksResponse: TalkCursorResponse ->
                talksResponse.toPresentation()
            }

    override suspend fun getAfterTalks(
        gameId: Long,
        after: Long?,
        limit: Int,
    ): Result<LivetalkResponseItem> =
        talkDataSource
            .getLatestTalks(
                gameId = gameId,
                after = after,
                limit = limit,
            ).map { talksResponse: TalkCursorResponse ->
                talksResponse.toPresentation()
            }

    override suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<LivetalkChatItem> =
        talkDataSource
            .postTalks(
                gameId = gameId,
                content = content,
            ).map { talkResponse: TalkResponse ->
                talkResponse.toPresentation()
            }

    override suspend fun deleteTalks(
        gameId: Long,
        talkId: Long,
    ): Result<Unit> =
        talkDataSource
            .deleteTalks(
                gameId = gameId,
                talkId = talkId,
            )

    override suspend fun reportTalks(talkId: Long): Result<Unit> =
        talkDataSource
            .reportTalks(
                talkId = talkId,
            )
}
