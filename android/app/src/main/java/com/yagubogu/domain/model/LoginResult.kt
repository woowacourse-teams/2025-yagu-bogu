package com.yagubogu.domain.model

sealed class LoginResult {
    data class Success(
        val message: String,
    ) : LoginResult()

    data object Cancel : LoginResult()

    data class Failure(
        val exception: Throwable?,
    ) : LoginResult()
}
