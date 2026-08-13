package com.yagubogu.ui.login.auth

interface OAuthCredentialManager {
    suspend fun getCredentialResult(): OAuthCredentialResult

    suspend fun signOut(): Result<Unit>
}
