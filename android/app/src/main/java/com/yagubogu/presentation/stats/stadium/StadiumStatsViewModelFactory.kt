package com.yagubogu.presentation.stats.stadium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StadiumStatsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StadiumStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StadiumStatsViewModel() as T
        }
        throw IllegalArgumentException()
    }
}
