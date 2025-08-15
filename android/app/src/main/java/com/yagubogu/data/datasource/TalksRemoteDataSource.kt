package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.TalksRequest
import com.yagubogu.data.dto.response.talks.TalkCursorResponse
import com.yagubogu.data.dto.response.talks.TalkDto
import com.yagubogu.data.service.TalksApiService
import com.yagubogu.data.util.safeApiCall

class TalksRemoteDataSource(
    private val talksApiService: TalksApiService,
) : TalksDataSource {
    override suspend fun getTalks(
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalkCursorResponse> =
        safeApiCall {
            talksApiService.getTalks(gameId, before, limit)
        }

    override suspend fun getLatestTalks(
        gameId: Long,
        after: Long?,
        limit: Int,
    ): Result<TalkCursorResponse> =
        when (after) {
            null ->
                safeApiCall {
                    talksApiService.getTalks(gameId, null, limit)
                }

            else ->
                safeApiCall {
                    talksApiService.getLatestTalks(gameId, after, limit)
                }
        }

    override suspend fun postTalks(
        gameId: Long,
        content: String,
    ): Result<TalkDto> =
        safeApiCall {
            talksApiService.postTalks(gameId, TalksRequest(content))
        }
}
