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

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleCredentialManager: GoogleCredentialManager,
) : ViewModel() {
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    fun signIn() {
        viewModelScope.launch {
            val googleCredentialResult: GoogleCredentialResult =
                googleCredentialManager.getGoogleCredentialResult()

            val loginResult: LoginResult =
                when (googleCredentialResult) {
                    is GoogleCredentialResult.Success -> {
                        val idToken: String = googleCredentialResult.idToken
                        authRepository.signIn(idToken)
                        LoginResult.Success("로그인 성공")
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
