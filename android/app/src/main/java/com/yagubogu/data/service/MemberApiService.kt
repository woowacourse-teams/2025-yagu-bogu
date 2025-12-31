package com.yagubogu.data.service

import com.yagubogu.data.dto.request.member.MemberFavoriteRequest
import com.yagubogu.data.dto.request.member.MemberNicknameRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlCompleteRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlStartRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse

interface MemberApiService {
    @GET("api/v1/members/me")
    suspend fun getMemberInfo(): HttpResponse

    @GET("api/v1/members/me/nickname")
    suspend fun getNickname(): HttpResponse

    @PATCH("api/v1/members/me/nickname")
    suspend fun patchNickname(
        @Body body: MemberNicknameRequest,
    ): HttpResponse

    @GET("api/v1/members/favorites")
    suspend fun getFavoriteTeam(): HttpResponse

    @PATCH("api/v1/members/favorites")
    suspend fun patchFavoriteTeam(
        @Body body: MemberFavoriteRequest,
    ): HttpResponse

    @DELETE("api/v1/members/me")
    suspend fun deleteMember(): HttpResponse

    @GET("api/v1/members/me/badges")
    suspend fun getBadges(): HttpResponse

    @PATCH("api/v1/members/me/badges/{badgeId}/representative")
    suspend fun patchRepresentativeBadge(
        @Path("badgeId") badgeId: Long,
    ): HttpResponse

    @POST("api/v1/members/me/profile-image/pre-signed")
    suspend fun postPresignedUrl(
        @Body request: PresignedUrlStartRequest,
    ): HttpResponse

    @POST("api/v1/members/me/profile-image/update")
    suspend fun postCompleteUpload(
        @Body request: PresignedUrlCompleteRequest,
    ): HttpResponse

    @GET("api/v1/members/{memberId}")
    suspend fun getMemberProfile(
        @Path("memberId") memberId: Long,
    ): HttpResponse
}
