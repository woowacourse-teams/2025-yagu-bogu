package com.yagubogu.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.auth.GoogleCredentialHandler

class LoginViewModelFactory(
    private val googleCredentialHandler: GoogleCredentialHandler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(googleCredentialHandler) as T
        }
        throw IllegalArgumentException()
    }
}
