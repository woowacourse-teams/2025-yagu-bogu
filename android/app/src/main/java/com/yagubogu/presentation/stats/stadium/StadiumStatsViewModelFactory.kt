package com.yagubogu.presentation.stats.stadium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.StatsRepository

class StadiumStatsViewModelFactory(
    private val statsRepository: StatsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StadiumStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StadiumStatsViewModel(statsRepository) as T
        }
        throw IllegalArgumentException()
    }
}
