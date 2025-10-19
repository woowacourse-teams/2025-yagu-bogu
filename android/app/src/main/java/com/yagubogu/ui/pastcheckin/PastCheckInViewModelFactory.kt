package com.yagubogu.ui.pastcheckin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.GameRepository

class PastCheckInViewModelFactory(
    private val gameRepository: GameRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PastCheckInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PastCheckInViewModel(gameRepository) as T
        }
        throw IllegalArgumentException()
    }
}
