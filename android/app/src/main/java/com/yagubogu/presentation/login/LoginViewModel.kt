package com.yagubogu.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.data.auth.GoogleCredentialResult
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.domain.repository.AuthRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
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
                                onSuccess = { result: LoginResult -> result },
                                onFailure = { exception: Throwable ->
                                    Timber.w(exception, "API 호출 실패")
                                    LoginResult.Failure(exception)
                                },
                            )
                    }

                    is GoogleCredentialResult.Failure -> LoginResult.Failure(googleCredentialResult.exception)
                    GoogleCredentialResult.Suspending -> LoginResult.Failure(null)
                    GoogleCredentialResult.Cancel -> LoginResult.Cancel
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
