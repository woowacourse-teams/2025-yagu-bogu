package com.yagubogu.presentation.stats.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.CheckInsRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class AttendanceHistoryViewModel(
    private val checkInsRepository: CheckInsRepository,
) : ViewModel() {
    // TODO : 페이지네이션 적용
    private val _attendanceHistoryItems = MutableLiveData<List<AttendanceHistoryItem>>()
    val attendanceHistoryItems: LiveData<List<AttendanceHistoryItem>> get() = _attendanceHistoryItems

    init {
        fetchAttendanceHistoryItems(2025, "ALL")
    }

    fun clearAttendanceHistoryItems() {
        _attendanceHistoryItems.value = emptyList()
    }

    fun fetchAttendanceHistoryItems(
        year: Int,
        result: String,
    ) {
        viewModelScope.launch {
            val attendanceHistories: Result<List<AttendanceHistoryItem>> =
                checkInsRepository.getCheckInHistories(year, result)
            attendanceHistories
                .onSuccess { attendanceHistoryItems: List<AttendanceHistoryItem> ->
                    _attendanceHistoryItems.value = attendanceHistoryItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }
}
