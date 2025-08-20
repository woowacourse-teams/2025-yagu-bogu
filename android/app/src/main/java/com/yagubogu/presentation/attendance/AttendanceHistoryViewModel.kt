package com.yagubogu.presentation.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.AttendanceHistoryOrder
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class AttendanceHistoryViewModel(
    private val checkInsRepository: CheckInsRepository,
) : ViewModel(),
    AttendanceHistorySummaryViewHolder.Handler,
    AttendanceHistoryDetailViewHolder.Handler {
    private val attendanceHistoryFilter = MutableLiveData(AttendanceHistoryFilter.ALL)
    private val _attendanceHistoryOrder = MutableLiveData(AttendanceHistoryOrder.LATEST)
    val attendanceHistoryOrder: LiveData<AttendanceHistoryOrder> get() = _attendanceHistoryOrder

    private val items: MutableLiveData<List<AttendanceHistoryItem.Detail>> =
        MediatorLiveData<List<AttendanceHistoryItem.Detail>>().apply {
            addSource(attendanceHistoryFilter) { fetchAttendanceHistoryItems() }
            addSource(_attendanceHistoryOrder) { fetchAttendanceHistoryItems() }
        }
    private val detailItemPosition = MutableLiveData<Int?>()

    val attendanceHistoryItems: LiveData<List<AttendanceHistoryItem>> =
        MediatorLiveData<List<AttendanceHistoryItem>>().apply {
            addSource(items) {
                detailItemPosition.value = FIRST_INDEX
                value = buildAttendanceHistoryItems()
            }
            addSource(detailItemPosition) { value = buildAttendanceHistoryItems() }
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

    fun switchAttendanceHistoryOrder() {
        _attendanceHistoryOrder.value =
            if (attendanceHistoryOrder.value == AttendanceHistoryOrder.LATEST) {
                AttendanceHistoryOrder.OLDEST
            } else {
                AttendanceHistoryOrder.LATEST
            }
    }

    override fun onSummaryItemClick(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        detailItemPosition.value = position
    }

    override fun onDetailItemClick(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        detailItemPosition.value = null
    }

    private fun buildAttendanceHistoryItems(): List<AttendanceHistoryItem> {
        val currentItems: List<AttendanceHistoryItem.Detail> = items.value.orEmpty()
        return currentItems.mapIndexed { index: Int, item: AttendanceHistoryItem.Detail ->
            if (index == detailItemPosition.value) item else item.summary
        }
    }

    companion object {
        private const val FIRST_INDEX = 0
    }
}
