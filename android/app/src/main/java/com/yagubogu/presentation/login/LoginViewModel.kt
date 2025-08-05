package com.yagubogu.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.LoginResult
import com.yagubogu.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    // TODO: 예외 디버깅용 임시 LiveData
    private val _login = MutableLiveData<String>()
    val login: LiveData<String> get() = _login

    fun signIn() {
        viewModelScope.launch {
            val result: LoginResult = authRepository.signInWithGoogle()
            _login.value =
                when (result) {
                    is LoginResult.Success -> result.message
                    LoginResult.Cancel -> "사용자가 취소했어요."
                    is LoginResult.Failure -> result.exception?.message ?: "예외 발생"
                }
        }
    }
}
