package com.yagubogu.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.token.TokenRepository
import com.yagubogu.ui.main.model.AutoLoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YaguBoguViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _autoLoginState = MutableStateFlow<AutoLoginState>(AutoLoginState.Loading)
    val autoLoginState: StateFlow<AutoLoginState> = _autoLoginState.asStateFlow()

    suspend fun isTokenValid(): Boolean = tokenRepository.refreshTokens().isSuccess

    suspend fun isNewUser(): Boolean = memberRepository.getFavoriteTeam().getOrNull() == null

    fun handleAutoLogin(onAppInitialized: () -> Unit) {
        viewModelScope.launch {
            if (!isTokenValid()) {
                _autoLoginState.emit(AutoLoginState.Failure)
                onAppInitialized()
                return@launch
            }
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)

            when (isNewUser()) {
                true -> _autoLoginState.emit(AutoLoginState.SignUp)
                false -> _autoLoginState.emit(AutoLoginState.SignIn)
            }
            onAppInitialized()
        }
    }
}
