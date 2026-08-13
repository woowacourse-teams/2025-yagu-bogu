package com.yagubogu.ui.login

interface AppleSignInDelegate {
    fun signIn(
        onSuccess: (idToken: String) -> Unit,
        onCancel: () -> Unit,
        onFailure: (message: String) -> Unit,
    )
}
