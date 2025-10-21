package com.yagubogu.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.AuthRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.ThirdPartyRepository

class SettingViewModelFactory(
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(memberRepository, authRepository, thirdPartyRepository) as T
        }
        throw IllegalArgumentException()
    }
}
