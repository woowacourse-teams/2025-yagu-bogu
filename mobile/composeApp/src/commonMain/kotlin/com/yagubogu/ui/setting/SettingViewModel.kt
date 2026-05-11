package com.yagubogu.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.geofence.GeofenceRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.member.NicknameUpdateError
import com.yagubogu.data.repository.member.toNicknameUpdateError
import com.yagubogu.data.repository.thirdparty.ThirdPartyRepository
import com.yagubogu.domain.util.now
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.setting.model.MemberInfoItem
import com.yagubogu.ui.setting.model.PresignedUrlCompleteItem
import com.yagubogu.ui.setting.model.PresignedUrlItem
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.util.UiText
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_edit_nickname_duplicate
import yagubogu.composeapp.generated.resources.setting_edit_nickname_invalid_format
import yagubogu.composeapp.generated.resources.setting_edit_nickname_member_not_found
import yagubogu.composeapp.generated.resources.setting_edit_nickname_network_error
import yagubogu.composeapp.generated.resources.setting_edit_nickname_no_permission
import yagubogu.composeapp.generated.resources.setting_edit_nickname_server_error
import yagubogu.composeapp.generated.resources.setting_edit_nickname_too_long
import yagubogu.composeapp.generated.resources.setting_edit_nickname_unknown_error
import yagubogu.composeapp.generated.resources.setting_edit_profile_image_processing_failed
import yagubogu.composeapp.generated.resources.setting_edit_profile_image_upload_failed
import kotlin.time.Clock

class SettingViewModel(
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val clock: Clock,
    private val geofenceRepository: GeofenceRepository,
) : ViewModel() {
    private val logger = Logger.withTag("SettingViewModel")

    private val _myMemberInfoItem = MutableStateFlow(MemberInfoItem())
    val myMemberInfoItem: StateFlow<MemberInfoItem> = _myMemberInfoItem.asStateFlow()

    private val _settingEvent =
        MutableSharedFlow<SettingEvent>(
            replay = 0, // 재수집 방지
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val settingEvent = _settingEvent.asSharedFlow()

    private val _geofenceNotification = MutableStateFlow(false)
    val geofenceNotification = _geofenceNotification.asStateFlow()

    init {
        observeGeofenceStatus()
    }

    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            memberRepository
                .updateNickname(newNickname)
                .onSuccess {
                    _myMemberInfoItem.value = myMemberInfoItem.value.copy(nickName = newNickname)
                    _settingEvent.emit(SettingEvent.NicknameEditSuccess(newNickname))
                }.onFailure { exception: Throwable ->
                    val nicknameUpdateError: NicknameUpdateError = exception.toNicknameUpdateError()

                    _settingEvent.emit(SettingEvent.NicknameEditFailure(nicknameUpdateError.toUiText()))
                    logger.w(exception) { "닉네임 변경 API 호출 실패" }
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
                    logger.w(exception) { "로그아웃 API 호출 실패" }
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
                    logger.w(exception) { "계정 삭제 API 호출 실패" }
                }
        }
    }

    fun cancelDeleteAccount() {
        viewModelScope.launch {
            _settingEvent.emit(SettingEvent.DeleteAccountCancel)
        }
    }

    fun handleProfileImage(uri: String) {
        viewModelScope.launch {
            runCatching {
                handleImagePickerKMPCroppedImage(
                    sourceImageUri = uri,
                    onUploadFailure = {
                        emitProfileError(UiText.StringRes(Res.string.setting_edit_profile_image_upload_failed))
                    },
                    onProcessingFailure = {
                        emitProfileError(UiText.StringRes(Res.string.setting_edit_profile_image_processing_failed))
                    },
                    onProfileImageUpload = ::uploadProfileImage,
                )
            }.onFailure { exception ->
                logger.e(exception) { "이미지 처리 과정 중 예외 발생" }
                emitProfileError(UiText.StringRes(Res.string.setting_edit_profile_image_processing_failed))
            }
        }
    }

    /**
     * 프로필 관련 에러 이벤트를 SharedFlow로 방출합니다.
     * 외부 라이브러리의 콜백에서도 안전하게 호출할 수 있도록 launch를 포함합니다.
     */
    fun emitProfileError(uiText: UiText) {
        viewModelScope.launch {
            _settingEvent.emit(SettingEvent.ProfileImageEditFailure(uiText))
        }
    }

    suspend fun uploadProfileImage(
        imageUri: String,
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
            logger.e(exception) { "프로필 이미지 업로드 실패" }
        }

    fun fetchMemberInfo() {
        viewModelScope.launch {
            memberRepository
                .getMemberInfo()
                .map { it.toUiModel(LocalDate.now(clock)) }
                .onSuccess { memberInfoItem: MemberInfoItem ->
                    _myMemberInfoItem.value = memberInfoItem
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "회원 정보 조회 API 호출 실패" }
                }
        }
    }

    fun updateGeofenceNotification(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                geofenceRepository.unregisterAll()
                geofenceRepository
                    .registerAll()
                    .onSuccess {
                        geofenceRepository.setGeofenceEnabled(true)
                    }.onFailure { logger.e(it) { "지오펜스 등록 실패" } }
            } else {
                geofenceRepository.unregisterAll()
                geofenceRepository.setGeofenceEnabled(false)
            }
        }
    }

    private fun observeGeofenceStatus() {
        viewModelScope.launch {
            geofenceRepository.isGeofenceEnabled().collect { enabled ->
                _geofenceNotification.value = enabled
            }
        }
    }

    private fun NicknameUpdateError.toUiText(): UiText =
        when (this) {
            NicknameUpdateError.DuplicateNickname -> UiText.StringRes(Res.string.setting_edit_nickname_duplicate)
            NicknameUpdateError.InvalidNickname -> UiText.StringRes(Res.string.setting_edit_nickname_invalid_format)
            NicknameUpdateError.MemberNotFound -> UiText.StringRes(Res.string.setting_edit_nickname_member_not_found)
            NicknameUpdateError.NoPermission -> UiText.StringRes(Res.string.setting_edit_nickname_no_permission)
            NicknameUpdateError.PayloadTooLarge -> UiText.StringRes(Res.string.setting_edit_nickname_too_long)
            NicknameUpdateError.ServerError -> UiText.StringRes(Res.string.setting_edit_nickname_server_error)
            NicknameUpdateError.NetworkIssue -> UiText.StringRes(Res.string.setting_edit_nickname_network_error)
            is NicknameUpdateError.Unknown ->
                message?.let { UiText.DynamicString(it) }
                    ?: UiText.StringRes(Res.string.setting_edit_nickname_unknown_error)
        }
}
