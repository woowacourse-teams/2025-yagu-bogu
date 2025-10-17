package com.yagubogu.data.service

import com.yagubogu.data.dto.request.member.MemberFavoriteRequest
import com.yagubogu.data.dto.request.member.MemberNicknameRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlCompleteRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlStartRequest
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MemberApiService {
    @GET("/api/v1/members/me")
    suspend fun getMemberInfo(): Response<MemberInfoResponse>

    @GET("/api/v1/members/me/nickname")
    suspend fun getNickname(): Response<MemberNicknameResponse>

    @PATCH("/api/v1/members/me/nickname")
    suspend fun patchNickname(
        @Body body: MemberNicknameRequest,
    ): Response<MemberNicknameResponse>

    @GET("/api/v1/members/favorites")
    suspend fun getFavoriteTeam(): Response<MemberFavoriteResponse>

    @PATCH("/api/v1/members/favorites")
    suspend fun patchFavoriteTeam(
        @Body body: MemberFavoriteRequest,
    ): Response<MemberFavoriteResponse>

    @DELETE("/api/v1/members/me")
    suspend fun deleteMember(): Response<Unit>

    @GET("/api/v1/members/me/badges")
    suspend fun getBadges(): Response<BadgeResponse>

    @PATCH("/api/v1/members/me/badges/{badgeId}/representative")
    suspend fun patchRepresentativeBadge(
        @Path("badgeId") badgeId: Long,
    ): Response<Unit>

    @POST("/api/v1/members/me/profile-image/pre-signed")
    suspend fun postPresignedUrl(
        @Body request: PresignedUrlStartRequest,
    ): Response<PresignedUrlStartResponse>

    @POST("/api/v1/members/me/profile-image/update")
    suspend fun postCompleteUpload(
        @Body request: PresignedUrlCompleteRequest,
    ): Response<PresignedUrlCompleteResponse>
}
