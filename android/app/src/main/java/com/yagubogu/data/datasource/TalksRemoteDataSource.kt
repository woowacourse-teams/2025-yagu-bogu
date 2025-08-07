package com.yagubogu.data.datasource

import com.yagubogu.data.dto.request.TalksRequest
import com.yagubogu.data.dto.response.talks.ContentDto
import com.yagubogu.data.dto.response.talks.TalkResponse
import com.yagubogu.data.service.TalksApiService
import com.yagubogu.data.util.safeApiCall

class TalksRemoteDataSource(
    private val talksApiService: TalksApiService,
) : TalksDataSource {
    override suspend fun getTalks(
        token: String,
        gameId: Long,
        before: Long?,
        limit: Int,
    ): Result<TalkResponse> =
        safeApiCall {
            talksApiService.getGames(token, gameId, before, limit)
        }

    override suspend fun postTalks(
        token: String,
        gameId: Long,
        content: String,
    ): Result<ContentDto> =
        safeApiCall {
            talksApiService.postTalks(token, gameId, TalksRequest(content))
        }
}
