package com.yagubogu.presentation.stats.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.domain.repository.StatsRepository

class StatsDetailViewModelFactory(
    private val statsRepository: StatsRepository,
    private val checkInRepository: CheckInRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsDetailViewModel(statsRepository, checkInRepository) as T
        }
        throw IllegalArgumentException()
    }
}
