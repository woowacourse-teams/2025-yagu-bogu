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
import com.yagubogu.ui.attendance.model.PastGameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _selectedMonth = MutableStateFlow<YearMonth>(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _filter = MutableStateFlow(AttendanceHistoryFilter.ALL)
    val filter: StateFlow<AttendanceHistoryFilter> = _filter.asStateFlow()

    private val _sort = MutableStateFlow(AttendanceHistorySort.LATEST)
    val sort: StateFlow<AttendanceHistorySort> = _sort.asStateFlow()

    private val _pastGameUiState = MutableStateFlow<PastGameUiState>(PastGameUiState.Loading)
    val pastGameUiState: StateFlow<PastGameUiState> = _pastGameUiState.asStateFlow()

    private val _pastCheckInUiEvent =
        MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val pastCheckInUiEvent: SharedFlow<Unit> = _pastCheckInUiEvent.asSharedFlow()

    fun fetchAttendanceHistoryItems() {
        viewModelScope.launch {
            val yearMonth: YearMonth = selectedMonth.value
            checkInRepository
                .getCheckInHistories(
                    yearMonth.year,
                    yearMonth.monthValue,
                    filter.value.name,
                    sort.value.name,
                ).mapList { it.toUiModel() }
                .onSuccess { attendanceItems: List<AttendanceHistoryItem> ->
                    _items.value = attendanceItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun fetchPastGames(date: LocalDate) {
        viewModelScope.launch {
            _pastGameUiState.value = PastGameUiState.Loading
            val gamesResult: Result<List<PastGameUiModel>> =
                gameRepository
                    .getGames(date)
                    .map { list: List<GameWithCheckInDto> ->
                        list.filter { !it.isMyCheckIn }
                    }.mapList { it.toAttendanceUiModel(date) }
            gamesResult
                .onSuccess { pastGameUiModels: List<PastGameUiModel> ->
                    _pastGameUiState.value = PastGameUiState.Success(pastGameUiModels)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun addPastCheckIn(gameId: Long) {
        viewModelScope.launch {
            checkInRepository
                .addPastCheckIn(gameId)
                .onSuccess {
                    _pastCheckInUiEvent.emit(Unit)
                    fetchAttendanceHistoryItems()
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun updateSelectedMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun updateFilter(filter: AttendanceHistoryFilter) {
        _filter.value = filter
    }

    fun updateSort(sort: AttendanceHistorySort) {
        _sort.value = sort
    }

    companion object {
        val START_MONTH: YearMonth = YearMonth.of(2021, 3)
        val END_MONTH: YearMonth = YearMonth.now()
    }
}
