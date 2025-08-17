package com.yagubogu.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.CheckInsRepository

class AttendanceHistoryViewModelFactory(
    private val checkInsRepository: CheckInsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceHistoryViewModel(checkInsRepository) as T
        }
        throw IllegalArgumentException()
    }
}
