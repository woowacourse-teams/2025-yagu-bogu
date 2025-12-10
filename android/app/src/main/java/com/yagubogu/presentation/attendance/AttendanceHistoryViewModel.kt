package com.yagubogu.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import com.yagubogu.presentation.attendance.model.AttendanceHistoryOrder
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository,
) : ViewModel() {
    private val items = MutableStateFlow<List<AttendanceHistoryItem>>(emptyList())

    private val attendanceHistoryFilter = MutableStateFlow(AttendanceHistoryFilter.ALL)
    private val attendanceHistoryOrder = MutableStateFlow(AttendanceHistoryOrder.LATEST)

    private val detailItemPosition = MutableStateFlow<Int?>(FIRST_INDEX)

    // 화면에 표시될 AttendanceHistoryItems
    val attendanceHistoryItems: StateFlow<List<AttendanceHistoryItem>> =
        combine(
            items,
            detailItemPosition,
        ) { _, _ ->
            buildAttendanceHistoryItems()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    init {
        fetchAttendanceHistoryItems()
    }

    fun fetchAttendanceHistoryItems(
        year: Int = LocalDate.now().year,
        filter: AttendanceHistoryFilter = attendanceHistoryFilter.value,
        order: AttendanceHistoryOrder = attendanceHistoryOrder.value,
    ) {
        viewModelScope.launch {
            checkInRepository
                .getCheckInHistories(year, filter.name, order.name)
                .mapList { it.toUiModel() }
                .onSuccess { attendanceHistoryItems: List<AttendanceHistoryItem> ->
                    items.value = attendanceHistoryItems
                    detailItemPosition.value = FIRST_INDEX
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun updateAttendanceHistoryFilter(filter: AttendanceHistoryFilter) {
        if (attendanceHistoryFilter.value != filter) {
            attendanceHistoryFilter.value = filter
            fetchAttendanceHistoryItems(filter = filter)
        }
    }

    fun switchAttendanceHistoryOrder() {
        attendanceHistoryOrder.value =
            when (attendanceHistoryOrder.value) {
                AttendanceHistoryOrder.LATEST -> AttendanceHistoryOrder.OLDEST
                AttendanceHistoryOrder.OLDEST -> AttendanceHistoryOrder.LATEST
            }
        fetchAttendanceHistoryItems(order = attendanceHistoryOrder.value)
    }

    fun onSummaryItemClick(item: AttendanceHistoryItem.Summary) {
        val position: Int = attendanceHistoryItems.value.indexOf(item)
        if (position < FIRST_INDEX) return
        detailItemPosition.value = position
    }

    fun onDetailItemClick(item: AttendanceHistoryItem.Detail) {
        val position: Int = attendanceHistoryItems.value.indexOf(item)
        if (position < FIRST_INDEX) return
        detailItemPosition.value = null
    }

    private fun buildAttendanceHistoryItems(): List<AttendanceHistoryItem> =
        items.value.mapIndexed { index: Int, item: AttendanceHistoryItem ->
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
