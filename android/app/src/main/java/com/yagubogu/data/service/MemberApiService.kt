package com.yagubogu.data.service

import com.yagubogu.data.dto.response.MemberFavoriteResponse
import retrofit2.Response
import retrofit2.http.GET

interface MemberApiService {
    @GET("/api/members/favorites")
    suspend fun getFavoriteTeam(): Response<MemberFavoriteResponse>
}
