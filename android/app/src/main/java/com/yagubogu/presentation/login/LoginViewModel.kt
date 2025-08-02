package com.yagubogu.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.auth.GoogleCredentialHandler
import kotlinx.coroutines.launch

class LoginViewModel(
    private val googleCredentialHandler: GoogleCredentialHandler,
) : ViewModel() {
    // TODO: 예외 디버깅용 임시 LiveData
    private val _login = MutableLiveData<String>()
    val login: LiveData<String> get() = _login

    fun signIn() {
        viewModelScope.launch {
            googleCredentialHandler.signIn(
                onSuccess = { _login.setValue(it) },
                onFailure = { _login.setValue(it) },
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleCredentialHandler.signOut()
        }
    }
}
