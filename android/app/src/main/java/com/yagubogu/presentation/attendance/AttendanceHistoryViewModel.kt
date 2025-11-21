package com.yagubogu.presentation.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.AttendanceHistoryOrder
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository,
) : ViewModel() {
    private var items: List<AttendanceHistoryItem> = emptyList()

    private val attendanceHistoryFilter = MutableLiveData(AttendanceHistoryFilter.ALL)
    private val _attendanceHistoryOrder = MutableLiveData(AttendanceHistoryOrder.LATEST)
    val attendanceHistoryOrder: LiveData<AttendanceHistoryOrder> get() = _attendanceHistoryOrder

    private val _detailItemPosition = MutableLiveData<Int?>(FIRST_INDEX)
    val detailItemPosition: LiveData<Int?> get() = _detailItemPosition

    val attendanceHistoryItems: LiveData<List<AttendanceHistoryItem>> =
        MediatorLiveData<List<AttendanceHistoryItem>>().apply {
            addSource(_detailItemPosition) { value = buildAttendanceHistoryItems() }
            addSource(attendanceHistoryFilter) { fetchAttendanceHistoryItems() }
            addSource(_attendanceHistoryOrder) { fetchAttendanceHistoryItems() }
        }

    fun fetchAttendanceHistoryItems(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val filter: AttendanceHistoryFilter =
                attendanceHistoryFilter.value ?: AttendanceHistoryFilter.ALL
            val order: AttendanceHistoryOrder =
                attendanceHistoryOrder.value ?: AttendanceHistoryOrder.LATEST

            checkInRepository
                .getCheckInHistories(year, filter.name, order.name)
                .mapList { it.toUiModel() }
                .onSuccess { attendanceHistoryItems: List<AttendanceHistoryItem> ->
                    items = attendanceHistoryItems
                    _detailItemPosition.value = FIRST_INDEX
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun updateAttendanceHistoryFilter(filter: AttendanceHistoryFilter) {
        if (attendanceHistoryFilter.value != filter) {
            attendanceHistoryFilter.value = filter
        }
    }

    fun switchAttendanceHistoryOrder() {
        _attendanceHistoryOrder.value =
            when (attendanceHistoryOrder.value) {
                AttendanceHistoryOrder.LATEST -> AttendanceHistoryOrder.OLDEST
                else -> AttendanceHistoryOrder.LATEST
            }
    }

    fun onSummaryItemClick(item: AttendanceHistoryItem.Summary) {
        val position: Int = attendanceHistoryItems.value.orEmpty().indexOf(item)
        if (position < FIRST_INDEX) return
        _detailItemPosition.value = position
    }

    fun onDetailItemClick(item: AttendanceHistoryItem.Detail) {
        val position: Int = attendanceHistoryItems.value.orEmpty().indexOf(item)
        if (position < FIRST_INDEX) return
        _detailItemPosition.value = null
    }

    private fun buildAttendanceHistoryItems(): List<AttendanceHistoryItem> =
        items.mapIndexed { index: Int, item: AttendanceHistoryItem ->
            when (item) {
                is AttendanceHistoryItem.Summary -> item
                is AttendanceHistoryItem.Detail -> if (index == detailItemPosition.value) item else item.summary
                is AttendanceHistoryItem.Canceled -> item
            }
        }

    companion object {
        private const val FIRST_INDEX = 0
    }
}
