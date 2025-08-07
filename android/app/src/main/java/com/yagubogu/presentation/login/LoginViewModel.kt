package com.yagubogu.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.data.auth.GoogleCredentialResult
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleCredentialManager: GoogleCredentialManager,
) : ViewModel() {
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    suspend fun isTokenValid(): Boolean = authRepository.refreshTokens().isSuccess

    fun signInWithGoogle() {
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
                                onSuccess = { LoginResult.Success },
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

    private fun signOutWithGoogle() {
        viewModelScope.launch {
            googleCredentialManager.signOut()
        }
    }
}
