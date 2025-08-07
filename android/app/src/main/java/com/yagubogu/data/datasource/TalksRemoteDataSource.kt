package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.talks.TalksResponse
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
    ): Result<TalksResponse> =
        safeApiCall {
            talksApiService.getGames(token, gameId, before, limit)
        }
}
