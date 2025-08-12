package com.yagubogu.presentation.stats.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.CheckInsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class AttendanceHistoryViewModel(
    private val checkInsRepository: CheckInsRepository,
) : ViewModel() {
    // TODO : 페이지네이션 적용
    private val _attendanceHistoryItems = MutableLiveData<List<AttendanceHistoryItem>>()
    val attendanceHistoryItems: LiveData<List<AttendanceHistoryItem>> get() = _attendanceHistoryItems

    init {
        fetchAttendanceHistoryItems(2025, AttendanceHistoryFilter.ALL)
    }

    fun fetchAttendanceHistoryItems(
        year: Int = LocalDate.now().year,
        filter: AttendanceHistoryFilter = AttendanceHistoryFilter.ALL,
    ) {
        viewModelScope.launch {
            val attendanceHistories: Result<List<AttendanceHistoryItem>> =
                checkInsRepository.getCheckInHistories(year, filter.name)
            attendanceHistories
                .onSuccess { attendanceHistoryItems: List<AttendanceHistoryItem> ->
                    _attendanceHistoryItems.value = attendanceHistoryItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }
}
