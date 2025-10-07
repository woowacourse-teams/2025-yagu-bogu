package com.yagubogu.data.datasource.member

import android.net.Uri
import com.yagubogu.data.dto.request.member.MemberCompleteRequest
import com.yagubogu.data.dto.request.member.MemberPresignedUrlRequest
import com.yagubogu.data.dto.response.member.MemberCompleteResponse
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.member.MemberPresignedUrlResponse
import com.yagubogu.domain.model.Team

interface MemberDataSource {
    suspend fun getMemberInfo(): Result<MemberInfoResponse>

    suspend fun getNickname(): Result<MemberNicknameResponse>

    suspend fun updateNickname(nickname: String): Result<MemberNicknameResponse>

    suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse>

    suspend fun updateFavoriteTeam(team: Team): Result<MemberFavoriteResponse>

    suspend fun deleteMember(): Result<Unit>

    suspend fun getPresignedProfileImageUrl(request: MemberPresignedUrlRequest): Result<MemberPresignedUrlResponse>

    suspend fun uploadProfileImage(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long
    ): Result<Unit>

    suspend fun postCompleteUploadProfileImage(request: MemberCompleteRequest): Result<MemberCompleteResponse>
}
