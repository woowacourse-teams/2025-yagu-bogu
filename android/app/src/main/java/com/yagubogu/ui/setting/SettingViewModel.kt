package com.yagubogu.ui.setting

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.thirdparty.ThirdPartyRepository
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.ui.setting.component.model.MemberInfoItem
import com.yagubogu.ui.setting.component.model.PresignedUrlCompleteItem
import com.yagubogu.ui.setting.component.model.PresignedUrlItem
import com.yagubogu.ui.setting.component.model.SettingDialogEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface SettingEvent {
    data class NicknameEdit(
        val newNickname: String,
    ) : SettingEvent

    data object Logout : SettingEvent

    data object DeleteAccount : SettingEvent

    data object DeleteAccountCancel : SettingEvent
}

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
) : ViewModel() {
    private val _settingTitle = MutableLiveData<String>()
    val settingTitle: LiveData<String> get() = _settingTitle

    private val _myMemberInfoItem = MutableStateFlow(MemberInfoItem())
    val myMemberInfoItem: StateFlow<MemberInfoItem> = _myMemberInfoItem.asStateFlow()

    private val _dialogEvent = MutableSharedFlow<SettingDialogEvent>()
    val dialogEvent: SharedFlow<SettingDialogEvent> = _dialogEvent.asSharedFlow()

    private val _settingEvent = MutableSharedFlow<SettingEvent>()
    val settingEvent: SharedFlow<SettingEvent> = _settingEvent.asSharedFlow()

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
                    _myMemberInfoItem.value = myMemberInfoItem.value.copy(nickName = newNickname)
                    _dialogEvent.emit(SettingDialogEvent.HideDialog)
                    _settingEvent.emit(SettingEvent.NicknameEdit(newNickname))
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
                    _settingEvent.emit(SettingEvent.Logout)
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
                    _settingEvent.emit(SettingEvent.DeleteAccount)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "계정 삭제 API 호출 실패")
                }
        }
    }

    fun cancelDeleteAccount() {
        viewModelScope.launch {
            _dialogEvent.emit(SettingDialogEvent.HideDialog)
            _settingEvent.emit(SettingEvent.DeleteAccountCancel)
        }
    }

    fun emitDialogEvent(event: SettingDialogEvent) {
        viewModelScope.launch {
            _dialogEvent.emit(event)
        }
    }

    fun hideDialog() {
        viewModelScope.launch {
            _dialogEvent.emit(SettingDialogEvent.HideDialog)
        }
    }

    suspend fun uploadProfileImage(
        imageUri: Uri,
        mimeType: String,
        size: Long,
    ): Result<Unit> =
        runCatching {
            // 1. Presigned URL 요청
            val presignedUrlItem: PresignedUrlItem =
                memberRepository.getPresignedUrl(mimeType, size).getOrThrow().toUiModel()

            // 2. S3 업로드
            thirdPartyRepository
                .uploadImageToS3(presignedUrlItem.url, imageUri, mimeType, size)
                .getOrThrow()

            // 3. Complete API 호출 및 프로필 업데이트
            val completeItem: PresignedUrlCompleteItem =
                memberRepository
                    .completeUploadProfileImage(presignedUrlItem.key)
                    .getOrThrow()
                    .toUiModel()
            _myMemberInfoItem.value =
                myMemberInfoItem.value.copy(profileImageUrl = completeItem.imageUrl)
        }.onFailure { exception: Throwable ->
            Timber.e(exception, "프로필 이미지 업로드 실패")
        }

    private fun fetchMemberInfo() {
        viewModelScope.launch {
            memberRepository
                .getMemberInfo()
                .map { it.toUiModel() }
                .onSuccess { memberInfoItem: MemberInfoItem ->
                    _myMemberInfoItem.value = memberInfoItem
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "회원 정보 조회 API 호출 실패")
                }
        }
    }
}
