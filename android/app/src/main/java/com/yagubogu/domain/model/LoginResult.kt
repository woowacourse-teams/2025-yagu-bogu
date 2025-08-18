package com.yagubogu.domain.model

sealed class LoginResult {
    data object SignUp : LoginResult()

    data object SignIn : LoginResult()

    data object Cancel : LoginResult()

    data class Failure(
        val exception: Throwable?,
    ) : LoginResult()
}
