package com.yagubogu.data.service

import com.yagubogu.data.dto.response.MemberFavoriteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MemberApiService {
    @GET("/api/members/{memberId}/favorites")
    suspend fun getFavoriteTeam(
        @Path("memberId") id: Long,
    ): Response<MemberFavoriteResponse>
}
