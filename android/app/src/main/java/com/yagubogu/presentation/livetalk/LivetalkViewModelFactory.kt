package com.yagubogu.presentation.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.GamesRepository

class LivetalkViewModelFactory(
    private val gamesRepository: GamesRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LivetalkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LivetalkViewModel(gamesRepository) as T
        }
        throw IllegalArgumentException()
    }
}
