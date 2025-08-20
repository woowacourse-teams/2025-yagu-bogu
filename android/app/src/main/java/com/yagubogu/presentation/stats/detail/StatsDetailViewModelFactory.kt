package com.yagubogu.presentation.stats.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StatsDetailViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsDetailViewModel() as T
        }
        throw IllegalArgumentException()
    }
}
