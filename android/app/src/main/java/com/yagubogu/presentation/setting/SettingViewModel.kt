package com.yagubogu.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _settingTitle = MutableLiveData<String>()
    val settingTitle: LiveData<String> get() = _settingTitle

    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> get() = _nickname

    private val _favoriteTeam = MutableLiveData<String>()
    val favoriteTeam: LiveData<String> get() = _favoriteTeam

    private val _nicknameEditedEvent = MutableSingleLiveData<String>()
    val nicknameEditedEvent: SingleLiveData<String> get() = _nicknameEditedEvent

    private val _logoutEvent = MutableSingleLiveData<Unit>()
    val logoutEvent: SingleLiveData<Unit> get() = _logoutEvent

    init {
        fetchNickname()
        fetchFavoriteTeam()
    }

    fun setSettingTitle(title: String) {
        _settingTitle.value = title
    }

    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            memberRepository
                .updateNickname(newNickname)
                .onSuccess {
                    _nickname.value = newNickname
                    _nicknameEditedEvent.setValue(newNickname)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "닉네임 변경 API 호출 실패")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            memberRepository
                .logout()
                .onSuccess {
                    _logoutEvent.setValue(Unit)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "로그아웃 API 호출 실패")
                }
        }
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
