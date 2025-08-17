package com.yagubogu.presentation.stats.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DetailStatsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailStatsViewModel() as T
        }
        throw IllegalArgumentException()
    }
}
