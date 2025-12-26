package com.yagubogu.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.presentation.mapper.toAttendanceUiModel
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.presentation.util.mapList
import com.yagubogu.ui.attendance.model.AttendanceHistoryFilter
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.AttendanceHistorySort
import com.yagubogu.ui.attendance.model.PastGameUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _items = MutableStateFlow<List<AttendanceHistoryItem>>(emptyList())
    val items: StateFlow<List<AttendanceHistoryItem>> = _items.asStateFlow()

    fun fetchAttendanceHistoryItems(
        yearMonth: YearMonth = YearMonth.now(),
        filter: AttendanceHistoryFilter = AttendanceHistoryFilter.ALL,
        sort: AttendanceHistorySort = AttendanceHistorySort.LATEST,
    ) {
        viewModelScope.launch {
            checkInRepository
                .getCheckInHistories(yearMonth.year, filter.name, sort.name)
                .mapList { it.toUiModel() }
                .onSuccess { attendanceItems: List<AttendanceHistoryItem> ->
                    _items.value = attendanceItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun fetchPastGames(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val gamesResult: Result<List<PastGameUiModel>> =
                gameRepository
                    .getGames(date)
                    .map { list: List<GameWithCheckInDto> ->
                        list.filter { !it.isMyCheckIn }
                    }.mapList { it.toAttendanceUiModel(date = date) }
            gamesResult
                .onSuccess { pastGameUiModels: List<PastGameUiModel> ->
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }
}
