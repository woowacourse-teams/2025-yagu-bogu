package com.yagubogu.domain.repository

import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.setting.MemberInfoItem

interface MemberRepository {
    suspend fun getMemberInfo(): Result<MemberInfoItem>

    suspend fun getNickname(): Result<String>

    suspend fun updateNickname(nickname: String): Result<Unit>

    suspend fun getFavoriteTeam(): Result<String?>

    suspend fun updateFavoriteTeam(team: Team): Result<Unit>

    suspend fun deleteMember(): Result<Unit>

    fun invalidateCache()
}
