package com.yagubogu.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Google 로그인 인증을 위한 Credential 요청 및 로그아웃을 담당하는 클래스.
 * - CredentialManager를 이용해 기존 계정으로 로그인 시도 또는 명시적 로그인 요청을 수행
 */
class GoogleCredentialRequestManager(
    private val context: Context,
    serverClientId: String,
    nonce: String,
) {
    private val credentialManager = CredentialManager.create(context)

    // 기존에 로그인한 구글 계정이 있다면 자동 로그인 시도
    private val googleIdOptionDefault: GetGoogleIdOption =
        GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(false) // 모든 계정 표시 (true로 하면 이전 로그인 계정만 표시)
            .setServerClientId(serverClientId)
            .setNonce(nonce)
            .build()

    // 명시적 로그인 옵션 (사용자에게 계정 선택 UI를 보여줌)
    private val signInWithGoogleOption: GetSignInWithGoogleOption =
        GetSignInWithGoogleOption
            .Builder(serverClientId = serverClientId)
            .setNonce(nonce)
            .build()

    // Credential 요청 객체 생성 (기본 옵션)
    private val credentialRequestWithDefault: GetCredentialRequest =
        buildCredentialRequest(googleIdOptionDefault)

    // Credential 요청 객체 생성 (명시적 로그인 옵션)
    private val credentialRequestWithSignIn: GetCredentialRequest =
        buildCredentialRequest(signInWithGoogleOption)

    /**
     * 기존 로그인된 계정으로부터 Credential 요청을 시도함
     */
    suspend fun getCredentialRequestWithDefault(): Result<GetCredentialResponse> = getCredentialResponseResult(credentialRequestWithDefault)

    /**
     * 명시적으로 로그인 UI를 띄워서 Credential 요청을 시도함
     */
    suspend fun getCredentialRequestWithSignIn(): Result<GetCredentialResponse> = getCredentialResponseResult(credentialRequestWithSignIn)

    /**
     * Credential 상태 초기화 (로그아웃)
     */
    suspend fun signOut() {
        val clearCredentialStateRequest = ClearCredentialStateRequest()
        credentialManager.clearCredentialState(clearCredentialStateRequest)
    }

    /**
     * 주어진 CredentialOption을 바탕으로 CredentialRequest 객체 생성
     */
    private fun buildCredentialRequest(option: CredentialOption): GetCredentialRequest =
        GetCredentialRequest
            .Builder()
            .addCredentialOption(option)
            .build()

    /**
     * Credential 요청을 비동기로 수행하고 결과를 Result로 래핑하여 반환
     * - 성공: Result.success(GetCredentialResponse)
     * - 실패: Result.failure(GetCredentialException)
     */
    private suspend fun getCredentialResponseResult(request: GetCredentialRequest): Result<GetCredentialResponse> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val response: GetCredentialResponse =
                    credentialManager.getCredential(context, request)
                Result.success(response)
            } catch (e: GetCredentialException) {
                Result.failure(e)
            }
        }
}
