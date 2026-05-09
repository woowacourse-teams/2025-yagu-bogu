package com.yagubogu.ui.onboarding.nickname

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.member.NicknameUpdateError
import com.yagubogu.data.repository.member.toNicknameUpdateError
import com.yagubogu.ui.mapper.text.toUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NicknameViewModel(
    private val memberRepository: MemberRepository,
    private val teamName: String,
) : ViewModel() {
    private val logger = Logger.withTag("NicknameViewModel")
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _isDuplicateChecked = MutableStateFlow(false)
    val isDuplicateChecked: StateFlow<Boolean> = _isDuplicateChecked.asStateFlow()

    private val _nicknameError = MutableStateFlow<String?>(null)
    val nicknameError: StateFlow<String?> = _nicknameError.asStateFlow()

    private val _navigateToMainEvent = MutableSharedFlow<Unit>()
    val navigateToMainEvent: SharedFlow<Unit> = _navigateToMainEvent.asSharedFlow()

    private var currentCheckedNickname: String? = null

    fun onNicknameChanged(newNickname: String) {
        _nickname.value = newNickname
        _nicknameError.value = null
        when {
            newNickname == currentCheckedNickname -> {
                _isDuplicateChecked.value = true
            }
            else -> {
                _isDuplicateChecked.value = false
            }
        }
    }

    fun updateNickname() {
        viewModelScope.launch {
            memberRepository
                .updateNickname(nickname.value)
                .onSuccess {
                    _isDuplicateChecked.value = true
                    currentCheckedNickname = nickname.value
                    _nicknameError.value = null
                }.onFailure { exception: Throwable ->
                    val nicknameUpdateError: NicknameUpdateError = exception.toNicknameUpdateError()
                    _nicknameError.value = nicknameUpdateError.toUiText().asString()
                    logger.w(exception) { "닉네임 변경 API 호출 실패" }
                }
        }
    }

    fun useDefaultNickname() {
        viewModelScope.launch {
            for (i in 1..3) {
                val result = memberRepository.updateNickname(generateNickname(teamName))

                if (result.isSuccess) {
                    _navigateToMainEvent.emit(Unit)
                    return@launch
                }

                val error = result.exceptionOrNull()?.toNicknameUpdateError()
                if (error != NicknameUpdateError.DuplicateNickname) {
                    logger.w(result.exceptionOrNull()) { "닉네임 변경 API 호출 실패 (닉네임 중복 외의 에러)" }
                    break
                }
            }

            repeat(2) {
                val result = memberRepository.updateNickname(generateFallbackNickname(teamName))
                result
                    .onSuccess {
                        _navigateToMainEvent.emit(Unit)
                        return@launch
                    }.onFailure { exception ->
                        logger.w(exception) { "fallback 닉네임 변경 API 호출 실패" }
                    }
            }

            logger.w { "닉네임 자동 생성 5회 모두 실패, 서버 기본 닉네임으로 진행" }
            _navigateToMainEvent.emit(Unit)
        }
    }

    fun onNextClick() {
        viewModelScope.launch {
            _navigateToMainEvent.emit(Unit)
        }
    }

    private fun generateNickname(teamName: String): String {
        repeat(10) {
            val adj1 = ADJECTIVES.random()
            val adj2 = ADJECTIVES.filter { it != adj1 }.random()
            val candidate = "${adj1.connective} ${adj2.attributive} ${teamName}팬"
            if (candidate.length <= 15) return candidate
        }
        return generateFallbackNickname(teamName)
    }

    private fun generateFallbackNickname(teamName: String): String {
        val randomSuffix =
            (1..4)
                .map { ('a'..'z').random() }
                .joinToString("")
        val adj = ADJECTIVES.random().attributive
        return "$adj ${teamName}팬$randomSuffix"
    }
}
