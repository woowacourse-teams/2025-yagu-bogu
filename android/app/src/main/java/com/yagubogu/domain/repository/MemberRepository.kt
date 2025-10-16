package com.yagubogu.domain.repository

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

    suspend fun addCompleteUploadProfileImage(key: String): Result<MemberCompleteItem>
}
