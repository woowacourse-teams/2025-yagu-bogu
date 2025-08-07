package com.yagubogu.domain.repository

import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem

interface TalksRepository {
    suspend fun getTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<List<LivetalkChatItem>>

    suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<LivetalkChatItem>
}
