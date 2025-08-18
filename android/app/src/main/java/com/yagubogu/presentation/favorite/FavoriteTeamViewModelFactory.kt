package com.yagubogu.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.MemberRepository

class FavoriteTeamViewModelFactory(
    private val memberRepository: MemberRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteTeamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteTeamViewModel(memberRepository) as T
        }
        throw IllegalArgumentException()
    }
}
