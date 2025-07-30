package com.yagubogu.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleLoginManager(
    serverClientId: String,
    nonce: String,
    private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)

    private val googleIdOption: GetGoogleIdOption =
        GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(false)
            // Google Cloud Console에서 발급받은 Web Client ID
            .setServerClientId(serverClientId)
            // 계정이 하나일 때 자동 선택
            .setAutoSelectEnabled(true)
            // (선택적) Google ID 토큰을 생성할 때 사용할 nonce 문자열, 백엔드에서 동일한 nonce로 검증
            .setNonce(nonce)
            .build()

    private val credentialRequest: GetCredentialRequest =
        GetCredentialRequest
            .Builder()
            .addCredentialOption(googleIdOption)
            .build()

    suspend fun signIn() {
        val result: Result<GetCredentialResponse> = requestCredential()
        result.onSuccess {
            handleCredential(result.getOrThrow())
        }
        result.onFailure {
            Log.e(TAG, it.message ?: "로그인에 실패했습니다.")
        }
    }

    suspend fun signOut() {
        val clearCredentialStateRequest = ClearCredentialStateRequest()
        credentialManager.clearCredentialState(clearCredentialStateRequest)
    }

    private suspend fun requestCredential(): Result<GetCredentialResponse> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val response: GetCredentialResponse =
                    credentialManager.getCredential(context, credentialRequest)
                Result.success(response)
            } catch (e: GetCredentialException) {
                Result.failure(e)
            }
        }

    private fun handleCredential(result: GetCredentialResponse) {
        // 정상적으로 반환된 Credential을 처리합니다.
        val credential = result.credential
        val responseJson: String

        when (credential) {
            // Passkey Credential 처리
            is PublicKeyCredential -> {
                // 이 responseJson(예: GetCredentialResponse)을 서버로 전송하여 검증 및 인증 처리
                responseJson = credential.authenticationResponseJson
                Log.d(TAG, "PublicKeyCredential, $responseJson")
            }

            // 사용자 이름과 비밀번호 기반 Credential 처리
            is PasswordCredential -> {
                // 서버에 사용자 이름과 비밀번호를 전송하여 검증 및 인증 처리
                val username = credential.id
                val password = credential.password
                Log.d(TAG, "PasswordCredential, $username, $password")
            }

            // 현재 코드에는 아래 방식만 적용되고 있습니다.
            // Google ID 토큰 기반 Credential 처리
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Google ID 토큰을 추출하여 서버로 전달해 검증 및 인증 처리
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential
                                .createFrom(credential.data)
                        // googleIdTokenCredential의 멤버들은 UI 목적용으로 사용할 수 있지만,
                        // 사용자 데이터 접근이나 저장에 직접 사용하면 안 됩니다.
                        // 반드시 토큰을 백엔드에서 검증해야 합니다:
                        // googleIdTokenCredential.getIdToken() 값을 서버에 전달
                        // 검증 가이드는 다음 문서 참고:
                        // https://developers.google.com/identity/gsi/web/guides/verify-google-id-token
                        Log.d(TAG, "CustomCredential, ${googleIdTokenCredential.id}")
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "유효하지 않은 Google ID 토큰 응답 수신", e)
                    }
                } else {
                    // 인식되지 않은 CustomCredential 타입 처리
                    Log.e(TAG, "예상치 못한 CustomCredential 타입")
                }
            }

            else -> {
                // 인식되지 않은 Credential 타입 처리
                Log.e(TAG, "예상치 못한 Credential 타입")
            }
        }
    }

    companion object {
        private const val TAG = "GoogleLoginManager"
    }
}
