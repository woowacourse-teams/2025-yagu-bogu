package com.yagubogu.auth

import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

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
     *
     * @param onSuccess 로그인 성공 시 ID 토큰 반환 콜백
     * @param onFailure 로그인 실패 시 메시지 반환 콜백
     */
    suspend fun signIn(
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        // 기존 로그인된 계정 우선 요청 (silent sign-in)
        val defaultResult = googleCredentialRequestManager.getCredentialRequestWithDefault()
        if (handleCredentialResponseResult(defaultResult, onSuccess, onFailure)) return

        // 실패 시 명시적 로그인 UI 요청 (explicit sign-in)
        val signInResult = googleCredentialRequestManager.getCredentialRequestWithSignIn()
        handleCredentialResponseResult(signInResult, onSuccess, onFailure)
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
     * - 실패 시: 예외 처리 및 콜백
     *
     * @return 처리 성공 여부 (true면 다음 단계 생략)
     */
    private fun handleCredentialResponseResult(
        result: Result<GetCredentialResponse>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ): Boolean =
        result.fold(
            onSuccess = { handleCredentialResponse(it, onSuccess, onFailure) },
            onFailure = { exception -> handleException(exception, onFailure) },
        )

    /**
     * Credential 응답에서 ID 토큰을 추출
     * - 유효한 CustomCredential 타입인지 확인
     * - GoogleIdTokenCredential 생성 시도
     *
     * @return 처리 성공 여부
     */
    private fun handleCredentialResponse(
        response: GetCredentialResponse,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ): Boolean {
        val credential = response.credential

        // Google ID 토큰이 담긴 CustomCredential인지 확인
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            logAndFail("유효하지 않은 Credential 타입입니다.", onFailure)
            return false
        }

        // ID 토큰 파싱 시도
        return try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Log.d(TAG, "ID Token: ${googleIdTokenCredential.idToken}")
            onSuccess(googleIdTokenCredential.idToken)
            true
        } catch (e: GoogleIdTokenParsingException) {
            logAndFail("ID 토큰 파싱 실패: ${e.message}", onFailure, e)
            false
        }
    }

    /**
     * Credential 요청 중 발생한 예외 처리
     *
     * @return true: 처리를 중단해야 함 / false: 다음 요청으로 넘어감
     */
    private fun handleException(
        exception: Throwable,
        onFailure: (String) -> Unit,
    ): Boolean =
        when (exception) {
            // 로그인 정보가 없는 경우 (계속 진행)
            is NoCredentialException -> false

            // 사용자가 로그인 UI를 취소한 경우 (진행 중단)
            is GetCredentialCancellationException -> true

            // 기타 예외는 로그와 함께 실패 처리
            else -> {
                logAndFail("Credential 요청 실패: ${exception.message}", onFailure, exception)
                false
            }
        }

    /**
     * 에러 로그 출력 후 실패 콜백 호출
     */
    private fun logAndFail(
        message: String,
        onFailure: (String) -> Unit,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
        onFailure(message)
    }

    companion object {
        private const val TAG = "GoogleCredentialHandler"
    }
}
