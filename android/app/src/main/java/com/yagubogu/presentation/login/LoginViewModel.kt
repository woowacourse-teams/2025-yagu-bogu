package com.yagubogu.presentation.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.BuildConfig
import com.yagubogu.auth.GoogleLoginManager
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
) : ViewModel() {
    private val googleLoginManager: GoogleLoginManager =
        GoogleLoginManager(BuildConfig.WEB_CLIENT_ID, "", application.applicationContext)

    fun signIn() {
        viewModelScope.launch {
            googleLoginManager.signIn()
        }
    }
}
