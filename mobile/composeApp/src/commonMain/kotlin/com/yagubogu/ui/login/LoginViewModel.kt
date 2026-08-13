package com.yagubogu.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.auth.LoginResultResponse
import com.yagubogu.data.repository.appconfig.AppConfigRepository
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.ui.home.model.MaintenanceInfo
import com.yagubogu.ui.login.auth.OAuthCredentialManager
import com.yagubogu.ui.login.auth.OAuthCredentialResult
import com.yagubogu.ui.login.model.LoginResult
import com.yagubogu.ui.login.model.OAuthProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
    private val appConfigRepository: AppConfigRepository,
) : ViewModel() {
    private val logger = Logger.withTag("LoginViewModel")

    private val _loginResult = MutableSharedFlow<LoginResult>()
    val loginResult: SharedFlow<LoginResult> = _loginResult.asSharedFlow()

    private val _maintenanceInfo: MutableStateFlow<MaintenanceInfo?> = MutableStateFlow(null)
    val maintenanceInfo: StateFlow<MaintenanceInfo?> = _maintenanceInfo.asStateFlow()

    init {
        viewModelScope.launch {
            _maintenanceInfo.value = appConfigRepository.getMaintenanceInfo()
        }
    }

    fun signInWithGoogle(credentialManager: OAuthCredentialManager) {
        viewModelScope.launch {
            _loginResult.emit(signIn(credentialManager, OAuthProvider.GOOGLE))
        }
    }

    fun signOutWithGoogle(credentialManager: OAuthCredentialManager) {
        viewModelScope.launch {
            credentialManager.signOut()
        }
    }

    fun signInWithApple(credentialManager: OAuthCredentialManager) {
        viewModelScope.launch {
            _loginResult.emit(signIn(credentialManager, OAuthProvider.APPLE))
        }
    }

    fun ignoreMaintenanceDialog(
        maintenanceId: Int,
        days: Int,
    ) {
        viewModelScope.launch {
            appConfigRepository.markMaintenanceDialogAsIgnored(maintenanceId, days)
        }
    }

    private suspend fun signIn(
        credentialManager: OAuthCredentialManager,
        provider: OAuthProvider,
    ): LoginResult {
        val credentialResult: OAuthCredentialResult = credentialManager.getCredentialResult()

        return when (credentialResult) {
            is OAuthCredentialResult.Success -> {
                authRepository
                    .login(idToken = credentialResult.idToken, provider = provider.name)
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
                            logger.w(exception) { "API 호출 실패" }
                            LoginResult.Failure(exception)
                        },
                    )
            }

            is OAuthCredentialResult.Failure -> LoginResult.Failure(credentialResult.exception)

            OAuthCredentialResult.Suspending -> LoginResult.Failure(null)

            OAuthCredentialResult.Cancel -> LoginResult.Cancel
        }
    }
}
