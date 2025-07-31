package com.yagubogu.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.auth.GoogleLoginManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val googleLoginManager: GoogleLoginManager,
) : ViewModel() {
    fun signIn() {
        viewModelScope.launch {
            googleLoginManager.signIn()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleLoginManager.signOut()
        }
    }
}
