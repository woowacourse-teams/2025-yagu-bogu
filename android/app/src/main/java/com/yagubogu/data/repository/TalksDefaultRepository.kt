package com.yagubogu.data.repository

import com.yagubogu.data.datasource.TalksDataSource
import com.yagubogu.data.dto.response.talks.TalkCursorResponse
import com.yagubogu.data.dto.response.talks.TalkDto
import com.yagubogu.domain.repository.TalksRepository
import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem
import com.yagubogu.presentation.livetalk.chat.LivetalkResponseItem

class TalksDefaultRepository(
    private val talksDataSource: TalksDataSource,
) : TalksRepository {
    override suspend fun getBeforeTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<LivetalkResponseItem> =
        talksDataSource
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
        talksDataSource
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
        talksDataSource
            .postTalks(
                gameId = gameId,
                content = content,
            ).map { talkDto: TalkDto ->
                talkDto.toPresentation()
            }
}
