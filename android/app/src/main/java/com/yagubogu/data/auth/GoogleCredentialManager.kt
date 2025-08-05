package com.yagubogu.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Google 로그인 인증을 위한 Credential 요청 및 로그아웃을 담당하는 클래스.
 * - CredentialManager를 이용해 기존 계정으로 로그인 시도 또는 명시적 로그인 요청을 수행
 */
class GoogleCredentialManager(
    private val context: Context,
    serverClientId: String,
    nonce: String,
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun fetchGoogleCredentialResult(): GoogleCredentialResult {
        // 기존 로그인된 계정 우선 요청 (silent sign-in)
        val googleIdOptionRequestResult: Result<GetCredentialResponse> =
            getCredentialRequestWithSignIn()
        val googleIdOptionCredentialResult: GoogleCredentialResult =
            handleGoogleCredentialResponseResult(googleIdOptionRequestResult)

        // 실패 시 명시적 로그인 UI 요청 (explicit sign-in)
        return if (googleIdOptionCredentialResult is GoogleCredentialResult.Suspending) {
            val signInRequestResult: Result<GetCredentialResponse> =
                getCredentialRequestWithSignIn()
            val signInCredentialResult: GoogleCredentialResult =
                handleGoogleCredentialResponseResult(signInRequestResult)
            signInCredentialResult
        } else {
            googleIdOptionCredentialResult
        }
    }

    /**
     * Credential 상태 초기화 (로그아웃)
     */
    suspend fun signOut(): Result<Boolean> {
        val clearCredentialStateRequest = ClearCredentialStateRequest()
        return runCatching {
            credentialManager.clearCredentialState(clearCredentialStateRequest)
            true
        }
    }

    /**
     * Credential 요청 결과를 처리
     * - 성공 시: Credential 파싱 시도
     * - 실패 시: GetCredentialException 발생 시, Suspending 전달
     */
    private fun handleGoogleCredentialResponseResult(result: Result<GetCredentialResponse>): GoogleCredentialResult =
        result.fold(
            onSuccess = { handleGoogleCredentialResponse(it) },
            onFailure = { handleGoogleCredentialException(it) },
        )

    /**
     * Credential 응답에서 ID 토큰을 추출
     * - 유효한 CustomCredential 타입인지 확인
     * - GoogleIdTokenCredential 생성 시도
     */
    private fun handleGoogleCredentialResponse(response: GetCredentialResponse): GoogleCredentialResult {
        val credential = response.credential

        // Google ID 토큰이 담긴 CustomCredential인지 확인
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleCredentialResult.Failure(NoCredentialException())
        }

        // ID 토큰 파싱 시도
        return try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            GoogleCredentialResult.Success(googleIdTokenCredential.idToken)
        } catch (e: Throwable) {
            handleGoogleCredentialException(e)
        }
    }

    /**
     * Credential 요청 중 발생한 예외 처리
     */
    private fun handleGoogleCredentialException(exception: Throwable): GoogleCredentialResult =
        when (exception) {
            // 사용자가 로그인 UI를 취소한 경우 (진행 중단)
            is GetCredentialCancellationException -> GoogleCredentialResult.Cancel
            // 로그인 정보가 없는 경우 (계속 진행)
            is GetCredentialException -> GoogleCredentialResult.Suspending
            else -> GoogleCredentialResult.Failure(exception)
        }

    // 기존에 로그인한 구글 계정이 있다면 자동 로그인 시도
    private val googleIdOption: GetGoogleIdOption =
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
    private val credentialRequestWithGoogleIdOption: GetCredentialRequest =
        buildCredentialRequest(googleIdOption)

    // Credential 요청 객체 생성 (명시적 로그인 옵션)
    private val credentialRequestWithSignIn: GetCredentialRequest =
        buildCredentialRequest(signInWithGoogleOption)

    /**
     * 기존 로그인된 계정으로부터 Credential 요청을 시도함
     */
    private suspend fun getCredentialRequestWithGoogleIdOption(): Result<GetCredentialResponse> =
        getCredentialResponseResult(credentialRequestWithGoogleIdOption)

    /**
     * 명시적으로 로그인 UI를 띄워서 Credential 요청을 시도함
     */
    private suspend fun getCredentialRequestWithSignIn(): Result<GetCredentialResponse> =
        getCredentialResponseResult(credentialRequestWithSignIn)

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
