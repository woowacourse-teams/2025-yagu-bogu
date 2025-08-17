package com.yagubogu.data.service

import com.yagubogu.data.dto.request.MemberFavoriteRequest
import com.yagubogu.data.dto.request.MemberLogoutRequest
import com.yagubogu.data.dto.request.MemberNicknameRequest
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface MemberApiService {
    @GET("/api/members/me/nickname")
    suspend fun getNickname(): Response<MemberNicknameResponse>

    @PATCH("/api/members/me/nickname")
    suspend fun patchNickname(
        @Body body: MemberNicknameRequest,
    ): Response<MemberNicknameResponse>

    @GET("/api/members/favorites")
    suspend fun getFavoriteTeam(): Response<MemberFavoriteResponse>

    @PATCH("/api/members/favorites")
    suspend fun patchFavoriteTeam(
        @Body body: MemberFavoriteRequest,
    ): Response<MemberFavoriteResponse>

    @POST("/api/auth/logout")
    suspend fun logout(
        @Body body: MemberLogoutRequest,
    ): Response<Unit>

    @DELETE("/api/members/me")
    suspend fun deleteMember(): Response<Unit>
}
