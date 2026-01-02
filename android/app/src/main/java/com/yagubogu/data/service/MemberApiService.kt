package com.yagubogu.data.service

import com.yagubogu.data.dto.request.member.MemberFavoriteRequest
import com.yagubogu.data.dto.request.member.MemberNicknameRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlCompleteRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlStartRequest
import com.yagubogu.data.dto.response.member.BadgeResponse
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.member.MemberProfileResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path

interface MemberApiService {
    @GET("api/v1/members/me")
    suspend fun getMemberInfo(): MemberInfoResponse

    @GET("api/v1/members/me/nickname")
    suspend fun getNickname(): MemberNicknameResponse

    @PATCH("api/v1/members/me/nickname")
    suspend fun patchNickname(
        @Body body: MemberNicknameRequest,
    ): MemberNicknameResponse

    @GET("api/v1/members/favorites")
    suspend fun getFavoriteTeam(): MemberFavoriteResponse

    @PATCH("api/v1/members/favorites")
    suspend fun patchFavoriteTeam(
        @Body body: MemberFavoriteRequest,
    ): MemberFavoriteResponse

    @DELETE("api/v1/members/me")
    suspend fun deleteMember()

    @GET("api/v1/members/me/badges")
    suspend fun getBadges(): BadgeResponse

    @PATCH("api/v1/members/me/badges/{badgeId}/representative")
    suspend fun patchRepresentativeBadge(
        @Path("badgeId") badgeId: Long,
    )

    @POST("api/v1/members/me/profile-image/pre-signed")
    suspend fun postPresignedUrl(
        @Body request: PresignedUrlStartRequest,
    ): PresignedUrlStartResponse

    @POST("api/v1/members/me/profile-image/update")
    suspend fun postCompleteUpload(
        @Body request: PresignedUrlCompleteRequest,
    ): PresignedUrlCompleteResponse

    @GET("api/v1/members/{memberId}")
    suspend fun getMemberProfile(
        @Path("memberId") memberId: Long,
    ): MemberProfileResponse
}
