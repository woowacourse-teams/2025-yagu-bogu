package com.yagubogu.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.auth.GoogleLoginManager

class LoginViewModelFactory(
    private val googleLoginManager: GoogleLoginManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(googleLoginManager) as T
        }
        throw IllegalArgumentException()
    }
}
