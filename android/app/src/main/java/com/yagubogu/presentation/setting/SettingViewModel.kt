package com.yagubogu.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.MemberRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> get() = _nickname

    init {
        fetchNickname()
    }

    private fun fetchNickname() {
        viewModelScope.launch {
            memberRepository
                .getNickname()
                .onSuccess { nickname: String ->
                    _nickname.value = nickname
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }
}
