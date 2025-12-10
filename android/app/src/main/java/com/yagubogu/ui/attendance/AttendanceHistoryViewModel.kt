package com.yagubogu.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository,
) : ViewModel() {
    private val _items = MutableStateFlow<List<AttendanceHistoryItem>>(emptyList())
    val items: StateFlow<List<AttendanceHistoryItem>> = _items.asStateFlow()

    private val _detailItemPosition = MutableStateFlow<Int?>(FIRST_INDEX)
    val detailItemPosition: StateFlow<Int?> = _detailItemPosition.asStateFlow()

    private val _attendanceHistoryFilter = MutableStateFlow(AttendanceHistoryFilter.ALL)
    val attendanceHistoryFilter: StateFlow<AttendanceHistoryFilter> =
        _attendanceHistoryFilter.asStateFlow()

    private val _attendanceHistorySort = MutableStateFlow(AttendanceHistorySort.LATEST)
    val attendanceHistorySort: StateFlow<AttendanceHistorySort> =
        _attendanceHistorySort.asStateFlow()

    init {
        fetchAttendanceHistoryItems()
    }

    fun fetchAttendanceHistoryItems(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val filter: AttendanceHistoryFilter = attendanceHistoryFilter.value
            val sort: AttendanceHistorySort = attendanceHistorySort.value
            checkInRepository
                .getCheckInHistories(year, filter.name, sort.name)
                .mapList { it.toUiModel() }
                .onSuccess { attendanceHistoryItems: List<AttendanceHistoryItem> ->
                    _items.value = attendanceHistoryItems
                    _detailItemPosition.value = FIRST_INDEX
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun updateAttendanceHistoryFilter(filter: AttendanceHistoryFilter) {
        if (attendanceHistoryFilter.value != filter) {
            _attendanceHistoryFilter.value = filter
            fetchAttendanceHistoryItems()
        }
    }

    fun switchAttendanceHistoryOrder() {
        _attendanceHistorySort.value =
            when (attendanceHistorySort.value) {
                AttendanceHistorySort.LATEST -> AttendanceHistorySort.OLDEST
                AttendanceHistorySort.OLDEST -> AttendanceHistorySort.LATEST
            }
        fetchAttendanceHistoryItems()
    }

    fun onItemClick(item: AttendanceHistoryItem) {
        val position: Int = items.value.indexOf(item)
        if (position < FIRST_INDEX) return

        if (position == detailItemPosition.value) {
            _detailItemPosition.value = null
        } else {
            _detailItemPosition.value = position
        }
    }

    companion object {
        private const val FIRST_INDEX = 0
    }
}
