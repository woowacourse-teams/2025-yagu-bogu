package com.yagubogu.data.service

import com.yagubogu.data.dto.request.talk.TalkRequest
import com.yagubogu.data.dto.response.talk.TalkCursorResponse
import com.yagubogu.data.dto.response.talk.TalkCursorResultIncludeTeamResponse
import com.yagubogu.data.dto.response.talk.TalkResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TalkApiService {
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
        @Body request: TalkRequest,
    ): Response<TalkResponse>

    @DELETE("/api/talks/{gameId}/{talkId}")
    suspend fun deleteTalks(
        @Path("gameId") gameId: Long,
        @Path("talkId") talkId: Long,
    ): Response<Unit>

    @POST("/api/talks/{talkId}/reports")
    suspend fun reportTalks(
        @Path("talkId") talkId: Long,
    ): Response<Unit>

    @GET("/api/talks/{gameId}")
    suspend fun getInitial(
        @Path("gameId") gameId: Long,
    ): Response<TalkCursorResultIncludeTeamResponse>
}
