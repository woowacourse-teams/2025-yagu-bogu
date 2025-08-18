package com.yagubogu.presentation.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.AttendanceHistorySort
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class AttendanceHistoryViewModel(
    private val checkInsRepository: CheckInsRepository,
) : ViewModel(),
    AttendanceHistorySummaryViewHolder.Handler,
    AttendanceHistoryDetailViewHolder.Handler {
    private val attendanceHistoryFilter = MutableLiveData(AttendanceHistoryFilter.ALL)
    private val _attendanceHistorySort = MutableLiveData(AttendanceHistorySort.NEWEST)
    val attendanceHistorySort: LiveData<AttendanceHistorySort> get() = _attendanceHistorySort

    private val items: MutableLiveData<List<AttendanceHistoryItem.Detail>> =
        MediatorLiveData<List<AttendanceHistoryItem.Detail>>().apply {
            addSource(attendanceHistoryFilter) { fetchAttendanceHistoryItems() }
            addSource(_attendanceHistorySort) { fetchAttendanceHistoryItems() }
        }
    private val detailItemIndex = MutableLiveData<Int?>()

    val attendanceHistoryItems: LiveData<List<AttendanceHistoryItem>> =
        MediatorLiveData<List<AttendanceHistoryItem>>().apply {
            addSource(items) {
                detailItemIndex.value = FIRST_INDEX
                value = buildAttendanceHistoryItems()
            }
            addSource(detailItemIndex) { value = buildAttendanceHistoryItems() }
        }

    fun fetchAttendanceHistoryItems(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val filter: AttendanceHistoryFilter =
                attendanceHistoryFilter.value ?: AttendanceHistoryFilter.ALL
            val attendanceHistories: Result<List<AttendanceHistoryItem.Detail>> =
                checkInsRepository.getCheckInHistories(year, filter.name)
            attendanceHistories
                .onSuccess { attendanceHistoryItems: List<AttendanceHistoryItem.Detail> ->
                    items.value = attendanceHistoryItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun updateAttendanceHistoryFilter(filter: AttendanceHistoryFilter) {
        attendanceHistoryFilter.value = filter
    }

    fun switchAttendanceHistorySort() {
        _attendanceHistorySort.value =
            if (attendanceHistorySort.value == AttendanceHistorySort.NEWEST) {
                AttendanceHistorySort.OLDEST
            } else {
                AttendanceHistorySort.NEWEST
            }
    }

    override fun onItemClick(item: AttendanceHistoryItem.Summary) {
        val index: Int = attendanceHistoryItems.value.orEmpty().indexOf(item)
        if (index != -1) {
            detailItemIndex.value = index
        }
    }

    override fun onItemClick(item: AttendanceHistoryItem.Detail) {
        val index: Int = attendanceHistoryItems.value.orEmpty().indexOf(item)
        if (index != -1) {
            detailItemIndex.value = null
        }
    }

    private fun buildAttendanceHistoryItems(): List<AttendanceHistoryItem> {
        val currentItems: List<AttendanceHistoryItem.Detail> = items.value.orEmpty()
        return currentItems.mapIndexed { index: Int, item: AttendanceHistoryItem.Detail ->
            if (index == detailItemIndex.value) item else item.summary
        }
    }

    companion object {
        private const val FIRST_INDEX = 0
    }
}
