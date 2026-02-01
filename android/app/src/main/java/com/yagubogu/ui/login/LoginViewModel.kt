package com.yagubogu.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.response.auth.LoginResultResponse
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import com.yagubogu.ui.login.auth.GoogleCredentialResult
import com.yagubogu.ui.login.model.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _loginResult = MutableSharedFlow<LoginResult>()
    val loginResult: SharedFlow<LoginResult> = _loginResult.asSharedFlow()

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
                                    val isNewUser: Boolean =
                                        memberRepository.getFavoriteTeam().getOrNull() == null

                                    when (result) {
                                        LoginResultResponse.SignUp -> LoginResult.SignUp
                                        LoginResultResponse.SignIn ->
                                            if (isNewUser) {
                                                LoginResult.SignUp
                                            } else {
                                                LoginResult.SignIn
                                            }
                                    }
                                },
                                onFailure = { exception: Throwable ->
                                    Timber.w(exception, "API 호출 실패")
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

            _loginResult.emit(loginResult)
        }
    }

    fun signOutWithGoogle(googleCredentialManager: GoogleCredentialManager) {
        viewModelScope.launch {
            googleCredentialManager.signOut()
        }
    }
}
