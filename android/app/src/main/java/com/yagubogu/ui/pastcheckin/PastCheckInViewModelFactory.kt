package com.yagubogu.ui.pastcheckin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.ui.badge.BadgeViewModel

class PastCheckInViewModelFactory(
    private val gameRepository: GameRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BadgeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PastCheckInViewModel(gameRepository) as T
        }
        throw IllegalArgumentException()
    }
}
