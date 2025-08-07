package com.yagubogu.data.service

import com.yagubogu.data.dto.request.TalksRequest
import com.yagubogu.data.dto.response.talks.ContentDto
import com.yagubogu.data.dto.response.talks.TalkResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TalksApiService {
    @GET("/api/talks/{gameId}")
    suspend fun getGames(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: Long,
        @Query("before") before: Long?,
        @Query("limit") limit: Int,
    ): Response<TalkResponse>

    @POST("/api/talks/{gameId}")
    suspend fun postTalks(
        @Header("Authorization") authorization: String,
        @Path("gameId") gameId: Long,
        @Body request: TalksRequest,
    ): Response<ContentDto>
}
