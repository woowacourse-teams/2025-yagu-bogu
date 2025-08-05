package com.yagubogu.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.data.repository.AuthDefaultRepository

class LoginViewModelFactory(
    private val authDefaultRepository: AuthDefaultRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authDefaultRepository) as T
        }
        throw IllegalArgumentException()
    }
}
