package com.yagubogu.domain.repository

import com.yagubogu.data.dto.response.auth.LoginResultResponse

interface AuthRepository {
    suspend fun login(idToken: String): Result<LoginResultResponse>

    suspend fun logout(): Result<Unit>
}
