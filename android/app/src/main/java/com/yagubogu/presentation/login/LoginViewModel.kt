package com.yagubogu.presentation.login

import android.content.Context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.auth.GoogleLoginManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val googleLoginManager: GoogleLoginManager,
) : ViewModel() {
    fun signIn(context: Context) {
        viewModelScope.launch {
            googleLoginManager.signIn(context)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleLoginManager.signOut()
        }
    }
}
