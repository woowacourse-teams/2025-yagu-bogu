package com.yagubogu.presentation.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.GameRepository

class LivetalkViewModelFactory(
    private val gameRepository: GameRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LivetalkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LivetalkViewModel(gameRepository) as T
        }
        throw IllegalArgumentException()
    }
}
