package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.TalksRepository

class LivetalkChatViewModelFactory(
    private val talksRepository: TalksRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LivetalkChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LivetalkChatViewModel(talksRepository) as T
        }
        throw IllegalArgumentException()
    }
}
