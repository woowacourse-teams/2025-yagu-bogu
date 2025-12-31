package com.yagubogu.data.service

import com.yagubogu.data.dto.request.talk.TalkRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse

interface TalkApiService {
    @GET("api/v1/talks/{gameId}")
    suspend fun getTalks(
        @Path("gameId") gameId: Long,
        @Query("before") before: Long?,
        @Query("limit") limit: Int,
    ): HttpResponse

    @GET("api/v1/talks/{gameId}/latest")
    suspend fun getLatestTalks(
        @Path("gameId") gameId: Long,
        @Query("after") after: Long,
        @Query("limit") limit: Int,
    ): HttpResponse

    @POST("api/v1/talks/{gameId}")
    suspend fun postTalks(
        @Path("gameId") gameId: Long,
        @Body request: TalkRequest,
    ): HttpResponse

    @DELETE("api/v1/talks/{gameId}/{talkId}")
    suspend fun deleteTalks(
        @Path("gameId") gameId: Long,
        @Path("talkId") talkId: Long,
    ): HttpResponse

    @POST("api/v1/talks/{talkId}/reports")
    suspend fun reportTalks(
        @Path("talkId") talkId: Long,
    ): HttpResponse

    @GET("api/v1/talks/{gameId}/initial")
    suspend fun getInitial(
        @Path("gameId") gameId: Long,
    ): HttpResponse
}
