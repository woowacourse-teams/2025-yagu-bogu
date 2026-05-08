package com.yagubogu.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.data.repository.appconfig.AppConfigRepository
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.ui.main.model.AutoLoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class YaguBoguViewModel(
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
    private val appConfigRepository: AppConfigRepository,
) : ViewModel() {
    private val logger = Logger.withTag("YaguBoguViewModel")
    private val _autoLoginState = MutableStateFlow<AutoLoginState>(AutoLoginState.Loading)
    val autoLoginState: StateFlow<AutoLoginState> = _autoLoginState.asStateFlow()

    private val _isMaintenance = MutableStateFlow(false)
    val isMaintenance: StateFlow<Boolean> = _isMaintenance.asStateFlow()

    private val _maintenanceMessage = MutableStateFlow("")
    val maintenanceMessage: StateFlow<String> = _maintenanceMessage.asStateFlow()

    fun handleAutoLogin(onAppInitialized: () -> Unit) {
        viewModelScope.launch {
            appConfigRepository.fetchConfigs()

            if (appConfigRepository.isMaintenanceMode()) {
                _isMaintenance.value = true
                _maintenanceMessage.value = appConfigRepository.getMaintenanceMessage()
                _autoLoginState.emit(AutoLoginState.Maintenance)
                logger.i { "점검 상태입니다. 점검 공지:${maintenanceMessage.value}" }
                onAppInitialized()
                return@launch
            }

            val isTokenValid: Boolean = authRepository.refreshToken().isSuccess
            if (!isTokenValid) {
                _autoLoginState.emit(AutoLoginState.Failure)
                onAppInitialized()
                return@launch
            }
            AnalyticsLogger.logEvent("login")

            val isNewUser: Boolean = memberRepository.getFavoriteTeam().getOrNull() == null
            when (isNewUser) {
                true -> _autoLoginState.emit(AutoLoginState.SignUp)
                false -> _autoLoginState.emit(AutoLoginState.SignIn)
            }
            onAppInitialized()
        }
    }
}
