package com.yagubogu.domain.repository

import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem

interface TalksRepository {
    suspend fun getTalks(
        token: String,
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<List<LivetalkChatItem>>

    suspend fun postTalks(
        token: String,
        gameId: Long,
        content: String,
    ): Result<LivetalkChatItem>
}
