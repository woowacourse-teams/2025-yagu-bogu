package com.yagubogu.domain.model

sealed class LoginResult {
    data object Success : LoginResult()

    data object Cancel : LoginResult()

    data class Failure(
        val exception: Throwable?,
    ) : LoginResult()
}
