package com.yagubogu.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.auth.GoogleCredentialHandler
import com.yagubogu.data.auth.GoogleCredentialResult
import kotlinx.coroutines.launch

class LoginViewModel(
    private val googleCredentialHandler: GoogleCredentialHandler,
) : ViewModel() {
    // TODO: 예외 디버깅용 임시 LiveData
    private val _login = MutableLiveData<String>()
    val login: LiveData<String> get() = _login

    fun signIn() {
        viewModelScope.launch {
            val result: GoogleCredentialResult = googleCredentialHandler.signIn()
            when (result) {
                is GoogleCredentialResult.Success -> _login.value = result.idToken
                is GoogleCredentialResult.Failure -> result.exception.toString()
                GoogleCredentialResult.Cancel -> _login.value = "사용자 취소"
                GoogleCredentialResult.Suspending -> Unit
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleCredentialHandler.signOut()
        }
    }
}
