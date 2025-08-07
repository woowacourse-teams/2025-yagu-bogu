package com.yagubogu.data.service

import com.yagubogu.data.dto.request.MemberFavoriteRequest
import com.yagubogu.data.dto.response.MemberFavoriteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface MemberApiService {
    @GET("/api/members/favorites")
    suspend fun getFavoriteTeam(): Response<MemberFavoriteResponse>

    @PATCH("/api/members/favorites")
    suspend fun patchFavoriteTeam(
        @Body body: MemberFavoriteRequest,
    ): Response<MemberFavoriteResponse>
}
