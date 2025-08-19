package com.yagubogu.data.service

import com.yagubogu.data.dto.request.TalksRequest
import com.yagubogu.data.dto.response.talks.TalkCursorResponse
import com.yagubogu.data.dto.response.talks.TalkDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TalksApiService {
    @GET("/api/talks/{gameId}")
    suspend fun getTalks(
        @Path("gameId") gameId: Long,
        @Query("before") before: Long?,
        @Query("limit") limit: Int,
    ): Response<TalkCursorResponse>

    @GET("/api/talks/{gameId}/latest")
    suspend fun getLatestTalks(
        @Path("gameId") gameId: Long,
        @Query("after") after: Long,
        @Query("limit") limit: Int,
    ): Response<TalkCursorResponse>

    @POST("/api/talks/{gameId}")
    suspend fun postTalks(
        @Path("gameId") gameId: Long,
        @Body request: TalksRequest,
    ): Response<TalkDto>
}
