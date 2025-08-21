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
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
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

    private val items = MutableLiveData<List<AttendanceHistoryItem.Detail>>()
    private val detailItemPosition = MutableLiveData<Int?>(FIRST_INDEX)

    val attendanceHistoryItems: LiveData<List<AttendanceHistoryItem>> =
        MediatorLiveData<List<AttendanceHistoryItem>>().apply {
            addSource(items) { value = buildAttendanceHistoryItems() }
            addSource(detailItemPosition) { value = buildAttendanceHistoryItems() }

            addSource(attendanceHistoryFilter) { fetchAttendanceHistoryItems() }
            addSource(_attendanceHistoryOrder) { fetchAttendanceHistoryItems() }
        }

    private val _scrollToTopEvent = MutableSingleLiveData<Unit>()
    val scrollToTopEvent: SingleLiveData<Unit> = _scrollToTopEvent

    fun fetchAttendanceHistoryItems(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            val filter: AttendanceHistoryFilter =
                attendanceHistoryFilter.value ?: AttendanceHistoryFilter.ALL
            val order: AttendanceHistoryOrder =
                attendanceHistoryOrder.value ?: AttendanceHistoryOrder.LATEST

            checkInsRepository
                .getCheckInHistories(year, filter.name, order.name)
                .onSuccess { attendanceHistoryItems: List<AttendanceHistoryItem.Detail> ->
                    items.value = attendanceHistoryItems
                    detailItemPosition.value = FIRST_INDEX
                    _scrollToTopEvent.setValue(Unit)
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
