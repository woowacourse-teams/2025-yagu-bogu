package com.yagubogu.presentation.livetalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.StadiumRepository

class LivetalkViewModelFactory(
    private val stadiumRepository: StadiumRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LivetalkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LivetalkViewModel(
                stadiumRepository,
            ) as T
        }
        throw IllegalArgumentException()
    }
}
