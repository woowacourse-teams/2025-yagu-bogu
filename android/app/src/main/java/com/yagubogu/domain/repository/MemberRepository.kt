package com.yagubogu.domain.repository

import android.net.Uri
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.setting.MemberCompleteItem
import com.yagubogu.presentation.setting.MemberInfoItem
import com.yagubogu.presentation.setting.MemberPresignedUrlItem

interface MemberRepository {
    suspend fun getMemberInfo(): Result<MemberInfoItem>

    suspend fun getNickname(): Result<String>

    suspend fun updateNickname(nickname: String): Result<Unit>

    suspend fun getFavoriteTeam(): Result<String?>

    suspend fun updateFavoriteTeam(team: Team): Result<Unit>

    suspend fun deleteMember(): Result<Unit>

    fun invalidateCache()

    suspend fun getPresignedProfileImageUrl(
        contentType: String,
        contentLength: Long,
    ): Result<MemberPresignedUrlItem>

    suspend fun updateProfileImage(
        url: String,
        imageFile: Uri,
        contentType: String,
        contentLength: Long,
    ): Result<Unit>

    suspend fun postCompleteUploadProfileImage(key: String): Result<MemberCompleteItem>
}
