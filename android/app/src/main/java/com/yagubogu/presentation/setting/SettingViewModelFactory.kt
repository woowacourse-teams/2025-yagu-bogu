package com.yagubogu.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.AuthRepository
import com.yagubogu.domain.repository.MemberRepository

class SettingViewModelFactory(
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(memberRepository, authRepository) as T
        }
        throw IllegalArgumentException()
    }
}
