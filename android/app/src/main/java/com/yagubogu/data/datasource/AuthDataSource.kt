package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.LoginResponse
import com.yagubogu.data.dto.response.RefreshResponse

interface AuthDataSource {
    suspend fun addLogin(idToken: String): Result<LoginResponse>

    suspend fun addRefresh(refreshToken: String): Result<RefreshResponse>
}
