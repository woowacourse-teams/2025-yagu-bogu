package com.yagubogu.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.auth.LoginResultResponse
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.token.TokenRepository
import com.yagubogu.presentation.login.auth.GoogleCredentialManager
import com.yagubogu.presentation.login.auth.GoogleCredentialResult
import com.yagubogu.presentation.login.model.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
    kermitLogger: Logger,
) : ViewModel() {
    val logger = kermitLogger.withTag("LoginViewModel")

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    suspend fun isTokenValid(): Boolean = tokenRepository.refreshTokens().isSuccess

    suspend fun isNewUser(): Boolean = memberRepository.getFavoriteTeam().getOrNull() == null

    fun signInWithGoogle(googleCredentialManager: GoogleCredentialManager) {
        viewModelScope.launch {
            val googleCredentialResult: GoogleCredentialResult =
                googleCredentialManager.getGoogleCredentialResult()

            val loginResult: LoginResult =
                when (googleCredentialResult) {
                    is GoogleCredentialResult.Success -> {
                        val idToken: String = googleCredentialResult.idToken
                        authRepository
                            .login(idToken)
                            .fold(
                                onSuccess = { result: LoginResultResponse ->
                                    when (result) {
                                        LoginResultResponse.SignUp -> LoginResult.SignUp
                                        LoginResultResponse.SignIn -> LoginResult.SignIn
                                    }
                                },
                                onFailure = { exception: Throwable ->
                                    logger.w(exception) { "API 호출 실패" }
                                    LoginResult.Failure(exception)
                                },
                            )
                    }

                    is GoogleCredentialResult.Failure -> {
                        LoginResult.Failure(googleCredentialResult.exception)
                    }

                    GoogleCredentialResult.Suspending -> {
                        LoginResult.Failure(null)
                    }

                    GoogleCredentialResult.Cancel -> {
                        LoginResult.Cancel
                    }
                }

            _loginResult.value = loginResult
        }
    }

    fun signOutWithGoogle(googleCredentialManager: GoogleCredentialManager) {
        viewModelScope.launch {
            googleCredentialManager.signOut()
        }
    }
}
