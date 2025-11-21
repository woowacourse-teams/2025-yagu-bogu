package com.yagubogu.domain.repository

import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkResponseItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkTeams

interface TalkRepository {
    suspend fun getBeforeTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<LivetalkResponseItem>

    suspend fun getAfterTalks(
        gameId: Long,
        after: Long?,
        limit: Int,
    ): Result<LivetalkResponseItem>

    suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<LivetalkChatItem>

    suspend fun deleteTalks(
        gameId: Long,
        talkId: Long,
    ): Result<Unit>

    suspend fun reportTalks(talkId: Long): Result<Unit>

    suspend fun getInitial(gameId: Long): Result<LivetalkTeams>
}
