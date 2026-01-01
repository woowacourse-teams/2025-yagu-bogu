package com.yagubogu.data.datasource.talk

import com.yagubogu.data.dto.request.talk.TalkRequest
import com.yagubogu.data.dto.response.talk.TalkCursorResponse
import com.yagubogu.data.dto.response.talk.TalkEntranceResponse
import com.yagubogu.data.dto.response.talk.TalkResponse
import com.yagubogu.data.service.TalkApiService
import com.yagubogu.data.util.safeApiCall
import javax.inject.Inject

class TalkRemoteDataSource @Inject constructor(
    private val talkApiService: TalkApiService,
) : TalkDataSource {
    override suspend fun getTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalkCursorResponse> =
        safeApiCall {
            talkApiService.getTalks(gameId, before, limit)
        }

    override suspend fun getLatestTalks(
        gameId: Long,
        after: Long?,
        limit: Int,
    ): Result<TalkCursorResponse> =
        safeApiCall {
            when (after) {
                null -> talkApiService.getTalks(gameId, null, limit)
                else -> talkApiService.getLatestTalks(gameId, after, limit)
            }
        }

    override suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<TalkResponse> =
        safeApiCall {
            talkApiService.postTalks(gameId, TalkRequest(content))
        }

    override suspend fun deleteTalks(
        gameId: Long,
        talkId: Long,
    ): Result<Unit> =
        safeApiCall {
            talkApiService.deleteTalks(gameId, talkId)
        }

    override suspend fun reportTalks(talkId: Long): Result<Unit> =
        safeApiCall {
            talkApiService.reportTalks(talkId)
        }

    override suspend fun getInitial(gameId: Long): Result<TalkEntranceResponse> =
        safeApiCall {
            talkApiService.getInitial(gameId)
        }
}
