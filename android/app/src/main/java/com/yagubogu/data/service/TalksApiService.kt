package com.yagubogu.data.service

import com.yagubogu.data.dto.response.talks.TalksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TalksApiService {
    @GET("/api/talks/{gameId}")
    suspend fun getGames(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: Long,
        @Query("before") before: Long?,
        @Query("limit") limit: Int,
    ): Response<TalksResponse>
}
