package com.yagubogu.domain.repository

import com.yagubogu.domain.model.LoginResult

interface AuthRepository {
    suspend fun signInWithGoogle(): LoginResult

    suspend fun signOutWithGoogle(): Result<Unit>
}
