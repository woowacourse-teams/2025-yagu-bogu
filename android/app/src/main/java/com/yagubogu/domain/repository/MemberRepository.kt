package com.yagubogu.domain.repository

interface MemberRepository {
    suspend fun getFavoriteTeam(): Result<String>
}
