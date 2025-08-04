package com.yagubogu.domain.repository

interface MemberRepository {
    suspend fun getFavoriteTeam(memberId: Long): Result<String>
}
