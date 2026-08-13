package com.yagubogu.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.member.NicknameUpdateError
import com.yagubogu.data.repository.member.toNicknameUpdateError
import com.yagubogu.data.repository.thirdparty.ThirdPartyRepository
import com.yagubogu.ui.common.model.PresignedUrlItem
import com.yagubogu.ui.mapper.text.toUiText
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.setting.model.MemberInfoItem
import com.yagubogu.ui.setting.model.PresignedUrlCompleteItem
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.util.CompressedImage
import com.yagubogu.ui.util.ImageCompressionSpec
import com.yagubogu.ui.util.UiText
import com.yagubogu.ui.util.compressImage
import com.yagubogu.ui.util.now
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.image_processing_failed
import yagubogu.composeapp.generated.resources.image_upload_failed
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

class SettingViewModel(
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val clock: Clock,
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
                compressImage(sourceUri = uri, spec = ImageCompressionSpec.Profile)
            }.fold(
                onSuccess = { image: CompressedImage ->
                    uploadProfileImage(image.uri, image.mimeType, image.fileSize)
                        .onFailure { emitProfileError(UiText.StringRes(Res.string.image_upload_failed)) }
                },
                onFailure = { exception ->
                    if (exception is CancellationException) throw exception
                    logger.e(exception) { "이미지 처리 과정 중 예외 발생" }
                    emitProfileError(UiText.StringRes(Res.string.image_processing_failed))
                },
            )
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
            if (exception is CancellationException) throw exception
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
}
