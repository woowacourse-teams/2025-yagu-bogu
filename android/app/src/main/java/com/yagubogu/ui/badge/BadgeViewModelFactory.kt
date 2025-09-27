package com.yagubogu.ui.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.MemberRepository

class BadgeViewModelFactory(
    private val memberRepository: MemberRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BadgeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BadgeViewModel(memberRepository) as T
        }
        throw IllegalArgumentException()
    }
}
