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

    private val _favoriteTeam = MutableLiveData<String>()
    val favoriteTeam: LiveData<String> get() = _favoriteTeam

    init {
        fetchNickname()
        fetchFavoriteTeam()
    }

    private fun fetchNickname() {
        viewModelScope.launch {
            memberRepository
                .getNickname()
                .onSuccess { nickname: String ->
                    _nickname.value = nickname
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "닉네임 조회 API 호출 실패")
                }
        }
    }

    private fun fetchFavoriteTeam() {
        viewModelScope.launch {
            memberRepository
                .getFavoriteTeam()
                .onSuccess { favoriteTeam: String ->
                    _favoriteTeam.value = favoriteTeam
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "응원팀 조회 API 호출 실패")
                }
        }
    }
}
