package com.yagubogu.auth

import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Google Credential 기반 로그인을 처리하는 클래스
 * - 기본 계정 자동 로그인 시도 후 실패 시 수동 로그인 요청
 * - Credential 응답을 파싱하여 ID 토큰을 추출
 */
class GoogleCredentialHandler(
    private val googleCredentialRequestManager: GoogleCredentialRequestManager,
) {
    /**
     * Google 로그인 시도
     */
    suspend fun signIn(): GoogleCredentialResult {
        // 기존 로그인된 계정 우선 요청 (silent sign-in)
        val googleIdOptionRequestResult =
            googleCredentialRequestManager.getCredentialRequestWithGoogleIdOption()
        val googleIdOptionCredentialResult =
            handleCredentialResponseResult(googleIdOptionRequestResult)

        // 실패 시 명시적 로그인 UI 요청 (explicit sign-in)
        return if (googleIdOptionCredentialResult is GoogleCredentialResult.Suspending) {
            val signInRequestResult =
                googleCredentialRequestManager.getCredentialRequestWithSignIn()
            handleCredentialResponseResult(signInRequestResult)
        } else {
            googleIdOptionCredentialResult
        }
    }

    /**
     * Credential 상태 초기화 (로그아웃)
     */
    suspend fun signOut() {
        googleCredentialRequestManager.signOut()
    }

    /**
     * Credential 요청 결과를 처리
     * - 성공 시: Credential 파싱 시도
     * - 실패 시: GetCredentialException 발생 시, Suspending 전달
     */
    private fun handleCredentialResponseResult(result: Result<GetCredentialResponse>): GoogleCredentialResult =
        result.fold(
            onSuccess = { handleCredentialResponse(it) },
            onFailure = { handleCredentialException(it) },
        )

    /**
     * Credential 응답에서 ID 토큰을 추출
     * - 유효한 CustomCredential 타입인지 확인
     * - GoogleIdTokenCredential 생성 시도
     */
    private fun handleCredentialResponse(response: GetCredentialResponse): GoogleCredentialResult {
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
            handleCredentialException(e)
        }
    }

    /**
     * Credential 요청 중 발생한 예외 처리
     */
    private fun handleCredentialException(exception: Throwable): GoogleCredentialResult =
        when (exception) {
            // 사용자가 로그인 UI를 취소한 경우 (진행 중단)
            is GetCredentialCancellationException -> GoogleCredentialResult.Cancel
            // 로그인 정보가 없는 경우 (계속 진행)
            is GetCredentialException -> GoogleCredentialResult.Suspending
            else -> GoogleCredentialResult.Failure(exception)
        }

    companion object {
        private const val TAG = "GoogleCredentialHandler"
    }
}
