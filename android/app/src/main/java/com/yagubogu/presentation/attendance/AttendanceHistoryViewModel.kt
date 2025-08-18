package com.yagubogu.presentation.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.presentation.attendance.model.AttendanceHistoryFilter
import com.yagubogu.presentation.attendance.model.AttendanceHistoryItem
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class AttendanceHistoryViewModel(
    private val checkInsRepository: CheckInsRepository,
) : ViewModel(),
    AttendanceHistorySummaryViewHolder.Handler,
    AttendanceHistoryDetailViewHolder.Handler {
    // TODO : 페이지네이션 적용
    private val items = MutableLiveData<List<AttendanceHistoryItem.Detail>>(emptyList())
    private val detailItemIndex = MutableLiveData<Int?>()

    val attendanceHistoryItems: LiveData<List<AttendanceHistoryItem>> =
        MediatorLiveData<List<AttendanceHistoryItem>>().apply {
            addSource(items) {
                detailItemIndex.value = FIRST_INDEX
                value = updateAttendanceHistoryItems()
            }
            addSource(detailItemIndex) { value = updateAttendanceHistoryItems() }
        }

    fun fetchAttendanceHistoryItems(
        year: Int = LocalDate.now().year,
        filter: AttendanceHistoryFilter = AttendanceHistoryFilter.ALL,
    ) {
        viewModelScope.launch {
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

    private fun updateAttendanceHistoryItems(): List<AttendanceHistoryItem> {
        val currentItems: List<AttendanceHistoryItem.Detail> = items.value.orEmpty()
        return currentItems.mapIndexed { index: Int, item: AttendanceHistoryItem.Detail ->
            if (index == detailItemIndex.value) item else item.summary
        }
    }

    companion object {
        private const val FIRST_INDEX = 0
    }
}
