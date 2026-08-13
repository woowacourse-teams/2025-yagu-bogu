package com.yagubogu.ui.login

import com.yagubogu.ui.login.auth.OAuthCredentialManager
import com.yagubogu.ui.login.auth.OAuthCredentialResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosAppleCredentialManager(
    private val delegate: AppleSignInDelegate,
) : OAuthCredentialManager {
    override suspend fun getCredentialResult(): OAuthCredentialResult =
        suspendCancellableCoroutine { continuation ->
            delegate.signIn(
                onSuccess = { idToken -> continuation.resume(OAuthCredentialResult.Success(idToken)) },
                onCancel = { continuation.resume(OAuthCredentialResult.Cancel) },
                onFailure = { message ->
                    continuation.resume(OAuthCredentialResult.Failure(Exception(message)))
                },
            )
        }

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
}
