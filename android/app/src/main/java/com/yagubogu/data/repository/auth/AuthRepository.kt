package com.yagubogu.data.repository.auth

import com.yagubogu.data.dto.response.auth.LoginResultResponse

interface AuthRepository {
    suspend fun login(idToken: String): Result<LoginResultResponse>

    suspend fun logout(): Result<Unit>
}
