package com.yagubogu.ui.login

import com.yagubogu.BuildKonfig
import com.yagubogu.ui.login.auth.OAuthCredentialManager
import com.yagubogu.ui.login.auth.OAuthCredentialResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosGoogleCredentialManager(
    private val delegate: GoogleSignInDelegate,
) : OAuthCredentialManager {
    init {
        // BuildKonfig의 클라이언트 ID를 Swift 델리게이트로 전달해 GIDSignIn을 구성한다.
        // 기존 CocoaPods 방식의 GIDConfiguration 설정을 Swift 측으로 위임한다.
        delegate.configure(
            iosClientId = BuildKonfig.IOS_CLIENT_ID,
            serverClientId = BuildKonfig.WEB_CLIENT_ID,
        )
    }

    override suspend fun getCredentialResult(): OAuthCredentialResult =
        suspendCancellableCoroutine { continuation ->
            delegate.signIn(
                onSuccess = { idToken -> continuation.resume(OAuthCredentialResult.Success(idToken)) },
                onCancel = { continuation.resume(OAuthCredentialResult.Cancel) },
                onFailure = { message -> continuation.resume(OAuthCredentialResult.Failure(Exception(message))) },
            )
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            delegate.signOut()
        }
}
