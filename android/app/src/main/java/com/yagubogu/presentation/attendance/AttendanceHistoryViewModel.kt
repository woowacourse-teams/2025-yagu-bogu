package com.yagubogu.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryOrder
import com.yagubogu.presentation.attendance.model.AttendanceHistoryUiModel
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val items = MutableStateFlow<List<AttendanceHistoryUiModel>>(emptyList())

    private val _detailItemPosition = MutableStateFlow<Int?>(FIRST_INDEX)
    val detailItemPosition: StateFlow<Int?> = _detailItemPosition.asStateFlow()

    // 화면에 표시될 AttendanceHistoryItems
    val attendanceHistoryItems: StateFlow<List<AttendanceHistoryUiModel>> =
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

    private val _attendanceHistoryFilter = MutableStateFlow(AttendanceHistoryFilter.ALL)
    val attendanceHistoryFilter: StateFlow<AttendanceHistoryFilter> =
        _attendanceHistoryFilter.asStateFlow()

    private val _attendanceHistorySort = MutableStateFlow(AttendanceHistoryOrder.LATEST)
    val attendanceHistorySort: StateFlow<AttendanceHistoryOrder> =
        _attendanceHistorySort.asStateFlow()

    init {
        fetchAttendanceHistoryItems()
    }

    fun fetchAttendanceHistoryItems(
        year: Int = LocalDate.now().year,
        filter: AttendanceHistoryFilter = attendanceHistoryFilter.value,
        order: AttendanceHistoryOrder = attendanceHistorySort.value,
    ) {
        viewModelScope.launch {
            checkInRepository
                .getCheckInHistories(year, filter.name, order.name)
                .mapList { it.toUiModel() }
                .onSuccess { attendanceHistoryUiModels: List<AttendanceHistoryUiModel> ->
                    items.value = attendanceHistoryUiModels
                    _detailItemPosition.value = FIRST_INDEX
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun updateAttendanceHistoryFilter(filter: AttendanceHistoryFilter) {
        if (attendanceHistoryFilter.value != filter) {
            _attendanceHistoryFilter.value = filter
            fetchAttendanceHistoryItems(filter = filter)
        }
    }

    fun switchAttendanceHistoryOrder() {
        _attendanceHistorySort.value =
            when (attendanceHistorySort.value) {
                AttendanceHistoryOrder.LATEST -> AttendanceHistoryOrder.OLDEST
                AttendanceHistoryOrder.OLDEST -> AttendanceHistoryOrder.LATEST
            }
        fetchAttendanceHistoryItems(order = attendanceHistorySort.value)
    }

    fun onItemClick(item: AttendanceHistoryUiModel) {
        val position: Int = attendanceHistoryItems.value.indexOf(item)
        if (position < FIRST_INDEX) return
        if (position == detailItemPosition.value) {
            _detailItemPosition.value = null
        } else {
            _detailItemPosition.value = position
        }
    }

    private fun buildAttendanceHistoryItems(): List<AttendanceHistoryUiModel> =
        items.value.mapIndexed { index: Int, item: AttendanceHistoryUiModel ->
            when (item) {
                is AttendanceHistoryUiModel.Summary -> item
                is AttendanceHistoryUiModel.Detail -> if (index == detailItemPosition.value) item else item.summary
                is AttendanceHistoryUiModel.Canceled -> item
            }
        }

    companion object {
        private const val FIRST_INDEX = 0
    }
}
