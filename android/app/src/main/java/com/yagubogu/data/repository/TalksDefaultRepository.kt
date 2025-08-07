package com.yagubogu.data.repository

import com.yagubogu.data.datasource.TalksDataSource
import com.yagubogu.data.dto.response.talks.ContentDto
import com.yagubogu.data.dto.response.talks.TalkResponse
import com.yagubogu.domain.repository.TalksRepository
import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem

class TalksDefaultRepository(
    private val talksDataSource: TalksDataSource,
) : TalksRepository {
    override suspend fun getTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<List<LivetalkChatItem>> =
        talksDataSource
            .getTalks(
                gameId = gameId,
                before = before,
                limit = limit,
            ).map { talksResponse: TalkResponse ->
                talksResponse.cursorResult.contents.map { it.toPresentation() }
            }

    override suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<LivetalkChatItem> =
        talksDataSource
            .postTalks(
                gameId = gameId,
                content = content,
            ).map { contentDto: ContentDto ->
                contentDto.toPresentation()
            }
}
