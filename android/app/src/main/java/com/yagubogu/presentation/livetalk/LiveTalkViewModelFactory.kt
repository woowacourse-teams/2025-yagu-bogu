package com.yagubogu.presentation.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.StadiumRepository

class LiveTalkViewModelFactory(
    private val stadiumRepository: StadiumRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveTalkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiveTalkViewModel(
                stadiumRepository,
            ) as T
        }
        throw IllegalArgumentException()
    }
}
