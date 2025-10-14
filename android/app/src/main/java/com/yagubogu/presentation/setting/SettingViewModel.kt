package com.yagubogu.presentation.setting

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.AuthRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

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
        fetchMemberInfo()
    }

    fun setSettingTitle(title: String) {
        _settingTitle.value = title
    }

    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            memberRepository
                .updateNickname(newNickname)
                .onSuccess {
                    _myMemberInfoItem.value = myMemberInfoItem.value?.copy(nickName = newNickname)
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
                    memberRepository.invalidateCache()
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

    suspend fun uploadProfileImageResult(
        imageUri: Uri,
        mimeType: String,
        size: Long,
    ): Result<Unit> =
        runCatching {
            // 1. Presigned URL 요청
            val presignedUrlItem: MemberPresignedUrlItem =
                memberRepository.getPresignedProfileImageUrl(mimeType, size).getOrThrow()

            // 2. S3 업로드
            memberRepository
                .updateProfileImage(presignedUrlItem.url, imageUri, mimeType, size)
                .getOrThrow()

            // 3. Complete API 호출 및 프로필 업데이트
            val complete: MemberCompleteItem =
                memberRepository.postCompleteUploadProfileImage(presignedUrlItem.key).getOrThrow()
            _myMemberInfoItem.value = myMemberInfoItem.value?.copy(profileImageUrl = complete.url)
        }.onFailure { exception: Throwable ->
            Timber.e(exception, "프로필 이미지 업로드 실패")
        }

    private fun fetchMemberInfo() {
        viewModelScope.launch {
            memberRepository
                .getMemberInfo()
                .onSuccess { memberInfoItem: MemberInfoItem ->
                    _myMemberInfoItem.value = memberInfoItem
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "회원 정보 조회 API 호출 실패")
                }
        }
    }
}
