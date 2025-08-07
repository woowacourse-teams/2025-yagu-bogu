package com.yagubogu.data.repository

import com.yagubogu.data.datasource.TalksDataSource
import com.yagubogu.data.dto.response.talks.TalksResponse
import com.yagubogu.domain.repository.TalksRepository
import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem

class TalksDefaultRepository(
    private val talksDataSource: TalksDataSource,
) : TalksRepository {
    override suspend fun getTalks(
        token: String,
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<List<LivetalkChatItem>> =
        talksDataSource
            .getTalks(
                token = token,
                gameId = gameId,
                before = before,
                limit = limit,
            ).map { talksResponse: TalksResponse ->
                talksResponse.contents.map { it.toPresentation() }
            }
}
