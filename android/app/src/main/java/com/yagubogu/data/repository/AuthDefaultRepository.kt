package com.yagubogu.data.repository

import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.data.auth.GoogleCredentialResult
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.domain.repository.AuthRepository

class AuthDefaultRepository(
    private val googleCredentialManager: GoogleCredentialManager,
) : AuthRepository {
    override suspend fun signInWithGoogle(): LoginResult {
        val googleCredentialResult: GoogleCredentialResult =
            googleCredentialManager.fetchGoogleCredentialResult()

        return when (googleCredentialResult) {
            is GoogleCredentialResult.Success -> LoginResult.Success("로그인 성공")
            is GoogleCredentialResult.Failure -> LoginResult.Failure(googleCredentialResult.exception)
            GoogleCredentialResult.Suspending -> LoginResult.Failure(null)
            GoogleCredentialResult.Cancel -> LoginResult.Cancel
        }
    }

    /**
     * Credential 상태 초기화 (로그아웃)
     */
    override suspend fun signOutWithGoogle(): Result<Boolean> = googleCredentialManager.signOut()
}
