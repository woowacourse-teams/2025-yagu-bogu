package com.yagubogu.ui.login

interface GoogleSignInDelegate {
    /**
     * GIDSignIn에 iOS/서버 클라이언트 ID를 설정한다.
     * IosGoogleCredentialManager 초기화 시 BuildKonfig에서 읽어 호출된다.
     */
    fun configure(
        iosClientId: String,
        serverClientId: String,
    )

    fun signIn(
        onSuccess: (idToken: String) -> Unit,
        onCancel: () -> Unit,
        onFailure: (message: String) -> Unit,
    )

    fun signOut()
}
