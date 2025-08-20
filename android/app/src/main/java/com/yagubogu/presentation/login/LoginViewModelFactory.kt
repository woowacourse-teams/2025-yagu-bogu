package com.yagubogu.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.data.auth.GoogleCredentialManager
import com.yagubogu.domain.repository.AuthRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.TokenRepository

class LoginViewModelFactory(
    private val tokenRepository: TokenRepository,
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
    private val googleCredentialManager: GoogleCredentialManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(
                tokenRepository,
                authRepository,
                memberRepository,
                googleCredentialManager,
            ) as T
        }
        throw IllegalArgumentException()
    }
}
