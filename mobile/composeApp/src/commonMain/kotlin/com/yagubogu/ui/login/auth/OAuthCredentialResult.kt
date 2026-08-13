package com.yagubogu.ui.login.auth

/**
 * OAuth 로그인 결과를 나타내는 sealed class.
 */
sealed class OAuthCredentialResult {
    /**
     * 로그인 성공 Result
     */
    data class Success(
        val idToken: String,
    ) : OAuthCredentialResult()

    /**
     * 현재 시도한 Login Option이 실패해서 다른 방식으로 재시도 할 때 사용하는 Result
     */
    data object Suspending : OAuthCredentialResult()

    /**
     * 로그인 실패 Result
     */
    data class Failure(
        val exception: Throwable?,
    ) : OAuthCredentialResult()

    /**
     * 사용자가 로그인 취소한 경우 Result
     */
    data object Cancel : OAuthCredentialResult()
}
