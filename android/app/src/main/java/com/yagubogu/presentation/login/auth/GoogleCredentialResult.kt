package com.yagubogu.presentation.login.auth

/**
 * Google Credential 로그인 결과를 나타내는 sealed class.
 */
sealed class GoogleCredentialResult {
    /**
     * 로그인 성공 Result
     */
    data class Success(
        val idToken: String,
    ) : GoogleCredentialResult()

    /**
     * 현재 시도한 Login Option이 실패해서 다른 방식으로 재시도 할 때 사용하는 Result
     */
    data object Suspending : GoogleCredentialResult()

    /**
     * 로그인 실패 Result
     */
    data class Failure(
        val exception: Throwable?,
    ) : GoogleCredentialResult()

    /**
     * 사용자가 로그인 취소한 경우 Result
     */
    data object Cancel : GoogleCredentialResult()
}
