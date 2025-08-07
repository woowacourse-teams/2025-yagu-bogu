package com.yagubogu.domain.repository

import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem
import com.yagubogu.presentation.livetalk.chat.LivetalkResponseItem

interface TalksRepository {
    suspend fun getTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<LivetalkResponseItem>

    suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<LivetalkChatItem>
}
