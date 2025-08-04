package com.yagubogu.auth

sealed class GoogleCredentialResult {
    data class Success(
        val idToken: String,
    ) : GoogleCredentialResult()

    data object Suspending : GoogleCredentialResult()

    data class Failure(
        val exception: Throwable?,
    ) : GoogleCredentialResult()

    data object Cancel : GoogleCredentialResult()
}
