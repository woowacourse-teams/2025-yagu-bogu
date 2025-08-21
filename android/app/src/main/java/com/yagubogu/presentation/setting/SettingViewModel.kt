package com.yagubogu.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.AuthRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class SettingViewModel(
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _settingTitle = MutableLiveData<String>()
    val settingTitle: LiveData<String> get() = _settingTitle

    private val _myMemberInfoItem = MutableLiveData<MemberInfoItem>()
    val myMemberInfoItem: LiveData<MemberInfoItem> get() = _myMemberInfoItem

    private val _nicknameEditedEvent = MutableSingleLiveData<String>()
    val nicknameEditedEvent: SingleLiveData<String> get() = _nicknameEditedEvent

    private val _logoutEvent = MutableSingleLiveData<Unit>()
    val logoutEvent: SingleLiveData<Unit> get() = _logoutEvent

    private val _deleteAccountEvent = MutableSingleLiveData<Unit>()
    val deleteAccountEvent: SingleLiveData<Unit> get() = _deleteAccountEvent

    private val _deleteAccountCancelEvent = MutableSingleLiveData<Unit>()
    val deleteAccountCancelEvent: SingleLiveData<Unit> get() = _deleteAccountCancelEvent

    init {
        // fetchMemberInfo()
        setDummyMemberInfo() // TODO: 내 정보 API 구현시 더미 제거
    }

    fun setSettingTitle(title: String) {
        _settingTitle.value = title
    }

    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            memberRepository
                .updateNickname(newNickname)
                .onSuccess {
                    _myMemberInfoItem.value = _myMemberInfoItem.value?.copy(nickName = newNickname)
                    _nicknameEditedEvent.setValue(newNickname)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "닉네임 변경 API 호출 실패")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository
                .logout()
                .onSuccess {
                    _logoutEvent.setValue(Unit)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "로그아웃 API 호출 실패")
                }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            memberRepository
                .deleteMember()
                .onSuccess {
                    _deleteAccountEvent.setValue(Unit)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "계정 삭제 API 호출 실패")
                }
        }
    }

    fun cancelDeleteAccount() {
        _deleteAccountCancelEvent.setValue(Unit)
    }

    private fun fetchMemberInfo() {
        viewModelScope.launch {
            memberRepository
                .getMemberInfo()
                .onSuccess { memberInfoResponse: MemberInfoResponse ->
                    _myMemberInfoItem.value = memberInfoResponse.toPresentation()
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "회원 정보 조회 API 호출 실패")
                }
        }
    }

    private fun setDummyMemberInfo() {
        _myMemberInfoItem.value =
            MemberInfoItem(
                nickName = "Jake",
                createdAt = LocalDate.of(2025, 8, 20),
                favoriteTeam = Team.KT,
                profileImageUrl = "https://avatars.githubusercontent.com/u/66577?v=4",
            )
    }
}
