package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.games.GamesResponse
import com.yagubogu.data.service.GamesApiService
import com.yagubogu.data.util.safeApiCall
import java.time.LocalDate

class GamesRemoteDataSource(
    private val gamesApiService: GamesApiService,
) : GamesDataSource {
    override suspend fun getGames(
        token: String,
        date: LocalDate,
    ): Result<GamesResponse> =
        safeApiCall {
            gamesApiService.getGames(token, date.toString())
        }
}
